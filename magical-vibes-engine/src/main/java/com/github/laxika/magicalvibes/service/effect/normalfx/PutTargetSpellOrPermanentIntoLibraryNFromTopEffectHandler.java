package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PutTargetSpellOrPermanentIntoLibraryNFromTopEffect}: tuck a spell or nonland
 * permanent into its owner's library N from the top. Spell removal is not countering — Guile and
 * "can't be countered" do not apply.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetSpellOrPermanentIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetSpellOrPermanentIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetSpellOrPermanentIntoLibraryNFromTopEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            int position = e.position();
            if (permanentRemovalService.removePermanentToLibraryPosition(gameData, target, position)) {
                String ordinal = ordinalPhrase(position);
                gameLogService.append(gameData, GameLog.builder()
                        .card(target.getCard())
                        .text(" is put " + ordinal + " its owner's library.")
                        .build());
                log.info("Game {} - {} put {} library (position {})",
                        gameData.id, target.getCard().getName(), ordinal, position);
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        putSpellIntoLibrary(gameData, targetId, e.position());
    }

    private void putSpellIntoLibrary(GameData gameData, UUID targetCardId, int position) {
        StackEntry target = gameQueryService.findStackEntryByCardId(gameData, targetCardId);
        if (target == null) {
            log.info("Game {} - Spell tuck target no longer on stack", gameData.id);
            return;
        }

        StackEntryType type = target.getEntryType();
        if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
            log.info("Game {} - Spell tuck ignores ability on stack", gameData.id);
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        // Copies cease to exist when leaving the stack (approx. CR 704.5e); tokens put into the
        // library are cleaned up by state-based actions.
        if (!target.isCopy()) {
            Card spell = target.getCard();
            UUID ownerId = spell.getOwnerId() != null ? spell.getOwnerId() : target.getOwnerId();
            List<Card> library = gameData.playerDecks.get(ownerId);
            int insertIndex = Math.min(position, library.size());
            library.add(insertIndex, spell);
        }

        String ordinal = ordinalPhrase(position);
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" is put " + ordinal + " its owner's library.")
                .build());
        log.info("Game {} - spell {} put {} library (position {})",
                gameData.id, target.getCard().getName(), ordinal, position);
    }

    private static String ordinalPhrase(int position) {
        return switch (position) {
            case 0 -> "on top of";
            case 1 -> "second from the top of";
            case 2 -> "third from the top of";
            default -> (position + 1) + "th from the top of";
        };
    }
}

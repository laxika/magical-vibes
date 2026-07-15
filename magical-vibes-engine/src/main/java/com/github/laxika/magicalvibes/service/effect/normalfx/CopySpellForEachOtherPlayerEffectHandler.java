package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopySpellForEachOtherPlayerEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        Card spellCard = spellSnapshot.getCard();

        // CR 706.2 — a spell that "can't be copied" is not copied by these effects either.
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(castingPlayerId)) continue;

            if (e.optional()) {
                PendingMayAbility mayCopy = new PendingMayAbility(
                        entry.getCard(),
                        playerId,
                        List.of(new CopyControllerCastSpellEffect(spellSnapshot, playerId)),
                        "Copy " + spellCard.getName() + "?"
                );
                gameData.pendingMayAbilities.add(mayCopy);
                continue;
            }

            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(spellSnapshot, copyCard, playerId, spellSnapshot.getTargetId());

            gameData.stack.add(copyEntry);

            String logMsg = "A copy of " + spellCard.getName() + " is created for "
                    + gameData.playerIdToName.get(playerId) + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));

            if (copyEntry.getTargetId() != null) {
                PendingMayAbility retargetAbility = new PendingMayAbility(
                        entry.getCard(),
                        playerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + spellCard.getName() + "?",
                        copyCard.getId()
                );
                gameData.pendingMayAbilities.addFirst(retargetAbility);
            }
        }

        log.info("Game {} - {} triggers, copying {} for each other player",
                gameData.id, entry.getCard().getName(), spellCard.getName());
    }
}

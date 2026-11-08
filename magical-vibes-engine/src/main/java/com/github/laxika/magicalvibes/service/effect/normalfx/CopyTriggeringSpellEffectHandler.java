package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final CopyControllerCastSpellEffectHandler copyControllerCastSpellEffectHandler;
    private final TargetRedirectionSupport targetRedirectionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        CopyTriggeringSpellEffect triggeringSpellEffect = (CopyTriggeringSpellEffect) effect;
        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (spell == null) {
            // The spell left the stack: it is copied as it last existed there (CR 608.2h).
            spell = entry.getTriggeringSpellSnapshot();
        }
        // A copy is copied too when it is the triggering spell: a countered copy of Trial and Error triggers its own
        // "copy it" (a cast trigger's spell is never a copy).
        if (spell == null) return;

        Card spellCard = spell.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (triggeringSpellEffect.retargetToSource()
                && (sourcePermanentId == null
                || !targetRedirectionSupport.isValidNewTargetForSpell(
                gameData, spell, sourcePermanentId, entry.getControllerId()))) {
            return;
        }

        if (!triggeringSpellEffect.retargetToSource()) {
            // The copy's controller is the spell's: the shared copy machinery makes the copies, with the effect's
            // modifications, and offers the new-target choice.
            copyControllerCastSpellEffectHandler.resolve(gameData, entry, new CopyControllerCastSpellEffect(
                    spell, spell.getControllerId(), triggeringSpellEffect.grantedKeywords(),
                    triggeringSpellEffect.additionalTypes(), triggeringSpellEffect.removedSupertypes(),
                    triggeringSpellEffect.tokenCopy(), triggeringSpellEffect.mayChooseNewTargets(),
                    triggeringSpellEffect.grantHasteToPermanentSpell(), false,
                    triggeringSpellEffect.startingLoyaltyFromX(),
                    triggeringSpellEffect.permanentSpellToken(), triggeringSpellEffect.sacrificeAtEndStep(),
                    triggeringSpellEffect.additionalSubtypes(), triggeringSpellEffect.powerOverride(),
                    triggeringSpellEffect.toughnessOverride(), triggeringSpellEffect.copies(),
                    triggeringSpellEffect.dynamicCopies()));
            return;
        }

        var copyCard = triggeringSpellEffect.tokenCopy()
                ? copySupport.createTokenCopyCard(spellCard)
                : copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spell, copyCard, entry.getControllerId(), spell.getTargetId());
        if (copyEntry.getTargetId() != null) {
            copyEntry.setTargetId(sourcePermanentId);
        }
        for (int i = 0; i < copyEntry.getDeclaredTargetIds().size(); i++) {
            copyEntry.replaceTargetIdAt(i, sourcePermanentId);
        }
        copySupport.addCopyToStack(gameData, copyEntry, (PendingMayAbility) null);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        log.info("Game {} - copy of {} created", gameData.id, spellCard.getName());
    }
}

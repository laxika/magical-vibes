package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCopySpellEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Tempt with Mayhem's sequential opponent copy choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemptingOfferCopySpellEffectHandler implements NormalEffectHandlerBean {

    private final CopyControllerCastSpellEffectHandler copySpellHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptingOfferCopySpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TemptingOfferCopySpellEffect offer = (TemptingOfferCopySpellEffect) effect;
        UUID controllerId = offer.abilityControllerId() != null
                ? offer.abilityControllerId() : entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        StackEntry spellSnapshot = offer.spellSnapshot();
        if (spellSnapshot == null) {
            spellSnapshot = findTargetSpell(gameData, entry.getTargetId());
            if (spellSnapshot == null) {
                return;
            }
            spellSnapshot = new StackEntry(spellSnapshot);
        }
        if (spellSnapshot.getCard() == null || spellSnapshot.getCard().isCantBeCopied()) {
            return;
        }

        copyFor(gameData, entry, spellSnapshot, controllerId);

        List<UUID> opponents = offer.remainingOpponentIds() == null
                ? new ArrayList<>(AnyOpponentMayTakeDamageSacrificeSourceEffectHandler
                        .apnapOpponents(gameData, controllerId))
                : new ArrayList<>(offer.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));
        if (!opponents.isEmpty()) {
            promptNext(gameData, entry.getCard(), new TemptingOfferCopySpellEffect(
                    spellSnapshot, List.copyOf(opponents), controllerId));
        }
    }

    public void promptNext(GameData gameData, Card sourceCard, TemptingOfferCopySpellEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                "Copy the spell? If you do, " + sourceCard.getName()
                        + "'s controller copies it again."));
        log.info("Game {} - offering {} the {} spell-copy choice", gameData.id,
                gameData.playerIdToName.get(opponentId), sourceCard.getName());
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               TemptingOfferCopySpellEffect effect, boolean accepted) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (accepted && entry != null && effect.spellSnapshot() != null) {
            copyFor(gameData, entry, effect.spellSnapshot(), ability.controllerId());
            copyFor(gameData, entry, effect.spellSnapshot(), effect.abilityControllerId());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new TemptingOfferCopySpellEffect(
                    effect.spellSnapshot(), List.copyOf(remaining), effect.abilityControllerId()));
        }
    }

    private void copyFor(GameData gameData, StackEntry sourceEntry,
                         StackEntry spellSnapshot, UUID controllerId) {
        if (controllerId == null) {
            return;
        }
        copySpellHandler.resolve(gameData, sourceEntry,
                new CopyControllerCastSpellEffect(spellSnapshot, controllerId));
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        return gameData.stack.stream()
                .filter(stackEntry -> targetId.equals(stackEntry.getTargetableId()))
                .findFirst()
                .orElse(null);
    }
}

package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "As it enters, you may reveal a [subtype] card; if you don't, it enters tapped."
 * (Lorwyn dual lands, e.g. Ancient Amphitheater). Declining taps the just-entered permanent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealSubtypeOrEntersTappedHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealSubtypeOrEntersTappedEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        RevealSubtypeOrEntersTappedEffect revealOrTapped = ability.effects().stream()
                .filter(e -> e instanceof RevealSubtypeOrEntersTappedEffect)
                .map(e -> (RevealSubtypeOrEntersTappedEffect) e)
                .findFirst().orElse(null);
        if (revealOrTapped != null) {
            if (accepted) {
                List<Card> hand = gameData.playerHands.get(ability.controllerId());
                List<Card> eligible = hand == null ? List.of() : hand.stream()
                        .filter(card -> revealOrTapped.subtypes().stream().anyMatch(subtype ->
                                gameQueryService.cardHasSubtype(card, subtype, gameData, ability.controllerId())))
                        .toList();
                if (eligible.size() > 1) {
                    interactionHandlerRegistry.begin(gameData,
                            new PendingInteraction.RevealedMatchingHandCardChoice(
                                    ability.controllerId(), ability.controllerId(), eligible, null,
                                    "Choose a card to reveal for " + ability.sourceCard().getName(), true, ability));
                    return;
                }
                Card revealed = eligible.isEmpty() ? null : eligible.getFirst();
                String revealedName = revealed != null ? revealed.getName() : revealOrTapped.subtypes().stream()
                        .map(subtype -> subtype.getDisplayName())
                        .findFirst()
                        .orElse("matching");
                gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " reveals " + revealedName + " — ", ability.sourceCard(), " enters untapped."));
                log.info("Game {} - {} reveals {} to keep {} untapped", gameData.id,
                        player.getUsername(), revealedName, ability.sourceCard().getName());
            } else {
                Permanent source = ability.sourcePermanentId() != null
                        ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
                if (source != null) {
                    source.tap();
                }
                gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines — ", ability.sourceCard(), " enters tapped."));
                log.info("Game {} - {} declines to reveal; {} enters tapped", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            if (ability.sourceCard().hasType(CardType.LAND)) {
                battlefieldEntryService.processLandETBEffects(gameData, ability.controllerId(), ability.sourceCard());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}

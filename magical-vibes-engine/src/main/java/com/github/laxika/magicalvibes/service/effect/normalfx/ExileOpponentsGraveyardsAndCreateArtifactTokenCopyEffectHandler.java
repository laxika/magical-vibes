package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect.class;
    }

    /**
     * On the spell, exiles each opponent's graveyard and queues the reflexive trigger's target
     * choice, which is made only once the spell has finished resolving and been put into its
     * owner's graveyard. On the reflexive trigger, creates the artifact-only token copy of the
     * chosen creature card if it is still exiled.
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            if (entry.getTargetId() != null) {
                createTokenCopyIfStillLegal(gameData, entry, entry.getTargetId());
            }
            return;
        }

        List<UUID> eligibleCardIds = exileOpponentsGraveyards(gameData, entry).stream()
                .filter(cardId -> isEligibleCreatureCard(gameData, cardId))
                .toList();
        if (!eligibleCardIds.isEmpty()) {
            gameData.pendingInteractions.addLast(new PendingInteraction.EspersToMagiciteCreatureChoice(
                    entry.getControllerId(), eligibleCardIds, entry.getCard()));
        }
    }

    private List<UUID> exileOpponentsGraveyards(GameData gameData, StackEntry entry) {
        List<UUID> exiledCardIds = new ArrayList<>();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(entry.getControllerId())) {
                continue;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(opponentId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            List<Card> exiledCards = new ArrayList<>(graveyard);
            graveyard.clear();
            graveyardService.notifyCardsExiledFromGraveyard(gameData, opponentId, exiledCards);
            for (Card card : exiledCards) {
                exileService.exileCard(gameData, opponentId, card);
                exiledCardIds.add(card.getId());
            }
        }
        return exiledCardIds;
    }

    private boolean isEligibleCreatureCard(GameData gameData, UUID cardId) {
        var exiled = gameData.findExiledCard(cardId);
        return exiled != null && !exiled.faceDown() && exiled.card().hasType(CardType.CREATURE);
    }

    private void createTokenCopyIfStillLegal(GameData gameData, StackEntry entry, UUID cardId) {
        if (!entry.getTargetCardIds().contains(cardId) || !isEligibleCreatureCard(gameData, cardId)) {
            return;
        }

        var exiled = gameData.findExiledCard(cardId);
        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        false,
                        List.of(
                                StaticAbility.of(List.of(
                                new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF)))));
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(exiled.card()), null, copyEffect);
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final int COMPLETED = -1;

    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEventValue() == COMPLETED) {
            return;
        }

        if (entry.getTargetId() != null) {
            createTokenCopyIfStillLegal(gameData, entry, entry.getTargetId());
            entry.setTargetId(null);
            entry.setEventValue(COMPLETED);
            return;
        }

        List<UUID> exiledCardIds = entry.getTargetCardIds();
        if (exiledCardIds.isEmpty()) {
            exiledCardIds = exileOpponentsGraveyards(gameData, entry);
            if (!exiledCardIds.isEmpty()) {
                queueReflexiveCopyAbility(gameData, entry, effect, exiledCardIds);
            }
            return;
        }

        List<UUID> eligibleCardIds = exiledCardIds.stream()
                .filter(cardId -> isEligibleCreatureCard(gameData, cardId))
                .toList();
        if (eligibleCardIds.isEmpty()) {
            entry.setEventValue(COMPLETED);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.EspersToMagiciteCreatureChoice(
                        entry.getControllerId(), eligibleCardIds));
    }

    private void queueReflexiveCopyAbility(GameData gameData, StackEntry entry,
                                           CardEffect effect, List<UUID> exiledCardIds) {
        StackEntry reflexiveAbility = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s reflexive ability",
                List.of(effect),
                entry.getSourcePermanentId(),
                List.of());
        reflexiveAbility.setTargetCardIds(exiledCardIds);
        reflexiveAbility.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        gameData.stack.add(reflexiveAbility);
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
                        Map.of(EffectSlot.STATIC, List.of(
                                new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF))));
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(exiled.card()), null, copyEffect);
    }
}

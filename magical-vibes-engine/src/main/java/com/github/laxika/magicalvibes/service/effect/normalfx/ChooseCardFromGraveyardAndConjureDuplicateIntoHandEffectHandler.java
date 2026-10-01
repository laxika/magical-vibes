package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/** Resolves Nashi's secret graveyard choice and conjures the selected duplicate. */
@Component
@RequiredArgsConstructor
public class ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromGraveyardAndConjureDuplicateIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            Card selectedCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
            if (selectedCard == null) {
                return;
            }
            if (!entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(
                    gameData, selectedCard.getId()))) {
                return;
            }
            conjureDuplicate(gameData, entry, selectedCard);
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of());
        if (graveyard.isEmpty()) {
            return;
        }
        if (graveyard.size() == 1) {
            conjureDuplicate(gameData, entry, graveyard.getFirst());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        entry.getControllerId(),
                        IntStream.range(0, graveyard.size()).boxed().toList(),
                        GraveyardChoiceDestination.CONJURE_DUPLICATE_INTO_HAND,
                        "Secretly choose a card from your graveyard to conjure into your hand.")
                .mandatory(true)
                .build());
    }

    private void conjureDuplicate(GameData gameData, StackEntry entry, Card selectedCard) {
        Card duplicate = selectedCard.createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        if (!duplicate.hasType(CardType.LAND)) {
            EnumSet<Keyword> keywords = duplicate.getKeywords().isEmpty()
                    ? EnumSet.noneOf(Keyword.class)
                    : EnumSet.copyOf(duplicate.getKeywords());
            keywords.add(Keyword.FLASH);
            duplicate.setKeywords(keywords);
            gameData.perpetualCardKeywords.put(duplicate.getId(), Set.of(Keyword.FLASH));
        }
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.text("A duplicate is secretly conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
    }
}

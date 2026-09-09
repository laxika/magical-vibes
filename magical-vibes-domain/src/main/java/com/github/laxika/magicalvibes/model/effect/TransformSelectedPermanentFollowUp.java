package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;

import java.util.List;
import java.util.UUID;

/** Offers to transform the permanent selected by a library-to-battlefield effect. */
public record TransformSelectedPermanentFollowUp() implements LibrarySelectionFollowUp {

    @Override
    public CardEffect createEffect(List<UUID> selectedPermanentIds) {
        return new TransformChosenPermanentEffect(
                new PermanentIsSpecificPermanentPredicate(selectedPermanentIds.getFirst()));
    }

    @Override
    public String prompt() {
        return "Transform that permanent?";
    }

    @Override
    public boolean shouldOffer(GameData gameData, List<UUID> selectedPermanentIds) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> selectedPermanentIds.contains(permanent.getId()))
                .anyMatch(this::canTransform);
    }

    private boolean canTransform(Permanent permanent) {
        Card originalCard = permanent.getOriginalCard();
        Card backFace = originalCard.getBackFaceCard();
        return !permanent.isTransformed()
                && backFace != null
                && backFace.getType() != null
                && backFace.getType().isPermanentType()
                && (originalCard.isModalDoubleFaced()
                || originalCard.hasType(CardType.BATTLE)
                || originalCard.getKeywords().contains(Keyword.TRANSFORM)
                || originalCard.getKeywords().contains(Keyword.DISTURB));
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/** Offers to transform the permanent selected by a library-to-battlefield effect. */
public record TransformSelectedPermanentFollowUp() implements LibrarySelectionFollowUp {

    @Override
    public CardEffect effect() {
        return new MayEffect(TransformChosenPermanentEffect.pickedPermanent(), prompt());
    }

    @Override
    public String prompt() {
        return "Transform that permanent?";
    }

    /** The "you may" is asked during the resolution that put the card onto the battlefield, not as a reflexive trigger. */
    @Override
    public boolean optional() {
        return false;
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

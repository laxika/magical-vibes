package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

/** Exiles up to one nonland permanent and up to one nonland permanent card, tracking both with the source. */
public record ExileTargetNonlandPermanentAndCardWithSourceEffect()
        implements CardEffect, BattlefieldAndGraveyardCardChoosingEffect {

    @Override
    public int mixedZoneMaxTargets() {
        return 2;
    }

    @Override
    public int mixedZoneMaxBattlefieldTargets() {
        return 1;
    }

    @Override
    public int mixedZoneMaxGraveyardTargets() {
        return 1;
    }

    @Override
    public PermanentPredicate mixedZoneBattlefieldPredicate() {
        return new PermanentNotPredicate(new PermanentIsLandPredicate());
    }

    @Override
    public CardPredicate mixedZoneGraveyardPredicate() {
        return new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }

    @Override
    public boolean mixedZoneExcludesSourcePermanent() {
        return false;
    }

    @Override
    public String mixedZoneChoiceDescription(int maxTargets) {
        return "target nonland permanent and/or nonland permanent card in a graveyard to exile.";
    }
}

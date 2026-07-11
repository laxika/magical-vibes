package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "42")
public class MothdustChangeling extends Card {

    public MothdustChangeling() {
        // Changeling is auto-loaded from Scryfall.
        // Tap an untapped creature you control: This creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "Tap an untapped creature you control: This creature gains flying until end of turn."
        ));
    }
}

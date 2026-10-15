package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "209")
public class CanopyDragon extends Card {

    public CanopyDragon() {
        // {1}{G}: This creature gains flying and loses trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new RemoveKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{1}{G}: Canopy Dragon gains flying and loses trample until end of turn."
        ));
    }
}

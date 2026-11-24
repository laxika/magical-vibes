package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "150")
public class AshlingsPrerogative extends Card {

    public AshlingsPrerogative() {
        // "As this enchantment enters, choose odd or even. (Zero is even.)"
        addStaticEffect(new ChooseManaValueParityOnEnterEffect());
        // "Each creature with mana value of the chosen quality has haste."
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.allCreatures(),
                new PermanentHasSourceChosenParityPredicate(true)));
        // "Each creature without mana value of the chosen quality enters tapped."
        addStaticEffect(EnterPermanentsOfTypesTappedEffect.matching(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentHasSourceChosenParityPredicate(false))),
                Subjects.eachPlayer()));
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "95")
public class MouserAttack extends Card {

    public MouserAttack() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 colorless Robot artifact creature token",
                        new CreateTokenEffect(1, "Robot", 1, 1, null, List.of(CardSubtype.ROBOT),
                                Set.of(), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +3/+0 and gains first strike until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(3, 0),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(), "Target must be a creature."))
        )));
    }
}

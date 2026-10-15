package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "12")
public class FightAsOne extends Card {

    public FightAsOne() {
        var humanCreatureYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                )),
                "Target must be a Human creature you control.");
        var nonHumanCreatureYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))
                )),
                "Target must be a non-Human creature you control.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target Human creature you control gets +1/+1 and gains indestructible until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(1, 1),
                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)
                        ),
                        humanCreatureYouControl),
                new ChooseOneEffect.ChooseOneOption(
                        "Target non-Human creature you control gets +1/+1 and gains indestructible until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(1, 1),
                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)
                        ),
                        nonHumanCreatureYouControl)
        )));
    }
}

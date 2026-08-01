package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "194")
public class SelesnyaCharm extends Card {

    public SelesnyaCharm() {
        // Choose one —
        // • Target creature gets +2/+2 and gains trample until end of turn.
        // • Exile target creature with power 5 or greater.
        // • Create a 2/2 white Knight creature token with vigilance.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+2 and gains trample until end of turn",
                        List.of(
                                new BoostTargetCreatureEffect(2, 2),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature with power 5 or greater",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(5))),
                                "Target must be a creature with power 5 or greater.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/2 white Knight creature token with vigilance",
                        new CreateTokenEffect(
                                "Knight", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of()))
        )));
    }
}

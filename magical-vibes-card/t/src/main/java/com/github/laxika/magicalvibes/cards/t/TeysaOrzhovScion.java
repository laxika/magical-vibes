package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "134")
@CardRegistration(set = "GK2", collectorNumber = "28")
@CardRegistration(set = "RVR", collectorNumber = "233")
public class TeysaOrzhovScion extends Card {

    public TeysaOrzhovScion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(3, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE))
                        ))),
                        new ExileTargetPermanentEffect()
                ),
                "Sacrifice three white creatures: Exile target creature.",
                TargetFilters.creature()
        ));

        addTrigger(Triggers.creatureDies(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                CreateTokenEffect.whiteSpirit(1));
    }
}

package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemLifeGainTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "214")
public class ProfessorDellianFel extends Card {

    public ProfessorDellianFel() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new GainLifeEffect(3)),
                "+2: You gain 3 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new DrawCardEffect(1), new LoseLifeEffect(1)),
                "0: You draw a card and lose 1 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "−3: Destroy target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemLifeGainTriggerEffect.Marker(List.of(
                                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER)
                        ))),
                        "Whenever you gain life, target opponent loses that much life."
                )),
                "−6: You get an emblem with \"Whenever you gain life, target opponent loses that much life.\""
        ));
    }
}

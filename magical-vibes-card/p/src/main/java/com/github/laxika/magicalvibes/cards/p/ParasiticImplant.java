package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "67")
public class ParasiticImplant extends Card {

    public ParasiticImplant() {
        // At the beginning of your upkeep, enchanted creature's controller sacrifices it and you create a 1/1
        // colorless Phyrexian Myr artifact creature token. The token doesn't depend on the sacrifice.
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                        new SacrificeSelfEffect(Subjects.attached(), Subjects.controllerOf(Subjects.attached())),
                        new CreateTokenEffect(
                                "Phyrexian Myr",
                                1, 1,
                                null,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR),
                                Set.of(),
                                Set.of(CardType.ARTIFACT))));
    }
}

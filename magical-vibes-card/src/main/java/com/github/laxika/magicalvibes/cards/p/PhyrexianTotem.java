package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "261")
public class PhyrexianTotem extends Card {

    public PhyrexianTotem() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new AnimatePermanentsEffect(
                        5, 5, List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR),
                        Set.of(Keyword.TRAMPLE), CardColor.BLACK)),
                "{2}{B}: This artifact becomes a 5/5 black Phyrexian Horror artifact creature with trample until end of turn."
        ));

        addEffect(EffectSlot.ON_DEALT_DAMAGE, new ConditionalEffect(
                new SourceIsCreature(),
                new SacrificePermanentsEffect(
                        new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER)));
    }
}

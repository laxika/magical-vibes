package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "849")
public class RavenousRotbelly extends Card {

    public RavenousRotbelly() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                SequenceEffect.of(
                        new SacrificeAnyNumberOfPermanentsEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), 3),
                        new ConditionalEffect(
                                new EventValueAtLeast(1),
                                new SacrificePermanentsEffect(
                                        new EventValue(),
                                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                                        SacrificeRecipient.EACH_OPPONENT))),
                "Sacrifice up to three Zombies?"));
    }
}

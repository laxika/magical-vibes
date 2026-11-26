package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "165")
@CardRegistration(set = "C14", collectorNumber = "303")
public class Karoo extends Card {

    private static final PermanentAllOfPredicate UNTAPPED_PLAINS = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
            new PermanentNotPredicate(new PermanentIsTappedPredicate())));

    public Karoo() {
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());

        // When this land enters, sacrifice it unless you return an untapped Plains you control to
        // its owner's hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(UNTAPPED_PLAINS, "untapped Plains you control"),
                new SacrificeSelfEffect()));

        // {T}: Add {C}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS), new AwardManaEffect(ManaColor.WHITE)),
                "{T}: Add {C}{W}."
        ));
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "167")
public class Anthropede extends Card {

    public Anthropede() {
        DestroyTargetPermanentEffect destroyRoom = new DestroyTargetPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ROOM));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Discard a card",
                                new DiscardCardThenEffect(null, destroyRoom, "a card")),
                        new ChooseOneEffect.ChooseOneOption("Pay {2}",
                                new MayPayManaEffect("{2}",
                                        new QueueReflexiveAbilityEffect(destroyRoom),
                                        "Pay {2} to destroy target Room?")))),
                "Discard a card or pay {2}?"));
    }
}

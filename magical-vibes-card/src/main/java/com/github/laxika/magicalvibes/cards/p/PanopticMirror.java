package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "136")
public class PanopticMirror extends Card {

    public PanopticMirror() {
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new MayEffect(
                        ExileFromHandToImprintEffect.withManaValueX(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                                "an instant or sorcery card with mana value X"),
                        "You may exile an instant or sorcery card with mana value X from your hand.")),
                "{X}, {T}: You may exile an instant or sorcery card with mana value X from your hand."));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CopyImprintedCardAndMayCastCopyEffect(false),
                "Copy a card exiled with Panoptic Mirror?"
        ));
    }
}

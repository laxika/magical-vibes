package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "188")
public class IsochronScepter extends Card {

    public IsochronScepter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileFromHandToImprintEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardMaxManaValuePredicate(2))),
                        "an instant card with mana value 2 or less"),
                        "You may exile an instant card with mana value 2 or less from your hand."));
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new CopyImprintedCardAndMayCastCopyEffect(false)),
                "{2}, {T}: You may copy the exiled card. If you do, you may cast the copy without paying its mana cost."));
    }
}

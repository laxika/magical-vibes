package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "1")
public class AetherShockwave extends Card {

    public AetherShockwave() {
        PermanentHasSubtypePredicate spirit = new PermanentHasSubtypePredicate(CardSubtype.SPIRIT);
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap all Spirits",
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, spirit)),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap all non-Spirit creatures",
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, new PermanentNotPredicate(spirit)))
        )));
    }
}

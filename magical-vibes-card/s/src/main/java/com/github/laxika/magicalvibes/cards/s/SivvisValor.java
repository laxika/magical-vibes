package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToTargetCreatureToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "22")
public class SivvisValor extends Card {

    public SivvisValor() {
        addCastingOption(new AlternateHandCast(
                List.of(new TapUntappedPermanentsCost(1, new PermanentIsCreaturePredicate())),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                false));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RedirectAllDamageToTargetCreatureToControllerEffect());
    }
}

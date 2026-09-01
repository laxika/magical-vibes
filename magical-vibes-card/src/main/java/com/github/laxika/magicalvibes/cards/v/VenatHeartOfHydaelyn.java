package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HydaelynTheMothercrystal;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "39")
@CardRegistration(set = "FIN", collectorNumber = "329")
@CardRegistration(set = "FIN", collectorNumber = "434")
public class VenatHeartOfHydaelyn extends Card {

    public VenatHeartOfHydaelyn() {
        setBackFaceCard(new HydaelynTheMothercrystal());

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        List.of(new DrawCardEffect()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new ExileTargetPermanentEffect(), new TransformSelfEffect()),
                "{7}, {T}: Exile target nonland permanent. Transform Venat. Activate only as a sorcery.",
                TargetFilters.nonlandPermanent(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "HydaelynTheMothercrystal";
    }
}

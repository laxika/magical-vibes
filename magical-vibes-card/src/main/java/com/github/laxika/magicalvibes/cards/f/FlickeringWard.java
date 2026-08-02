package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "19")
public class FlickeringWard extends Card {

    public FlickeringWard() {
        // Enchant creature; as it enters choose a color, and the enchanted creature has protection
        // from it (the grant never detaches this Aura). {W} bounces the Aura back to hand so the
        // colour can be re-chosen on the next cast.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect())
                .addEffect(EffectSlot.STATIC,
                        new ProtectionFromChosenColorEffect(GrantScope.ENCHANTED_CREATURE));
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(ReturnToHandEffect.self()),
                "{W}: Return Flickering Ward to its owner's hand."));
    }
}

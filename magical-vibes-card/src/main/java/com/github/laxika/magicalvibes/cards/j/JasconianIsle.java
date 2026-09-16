package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "MB1", collectorNumber = "117")
public class JasconianIsle extends Card {

    public JasconianIsle() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{U}{U}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {U}{U} to untap Jasconian Isle?"));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}

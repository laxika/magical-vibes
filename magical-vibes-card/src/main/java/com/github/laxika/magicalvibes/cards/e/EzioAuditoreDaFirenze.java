package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreerunningToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ACR", collectorNumber = "25")
@CardRegistration(set = "ACR", collectorNumber = "113")
@CardRegistration(set = "ACR", collectorNumber = "131")
public class EzioAuditoreDaFirenze extends Card {

    public EzioAuditoreDaFirenze() {
        addEffect(EffectSlot.STATIC, new GrantFreerunningToSpellsEffect(
                "{B}{B}", new CardSubtypePredicate(CardSubtype.ASSASSIN)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ConditionalEffect.unless(
                        new TargetPlayerLifeAtMost(10),
                        new MayPayManaEffect(
                                "{W}{U}{B}{R}{G}",
                                new TargetPlayerLosesGameEffect(null),
                                "Pay {W}{U}{B}{R}{G} to make that player lose the game?")));
    }
}

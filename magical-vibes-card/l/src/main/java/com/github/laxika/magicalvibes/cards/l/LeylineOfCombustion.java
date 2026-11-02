package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "148")
public class LeylineOfCombustion extends Card {

    public LeylineOfCombustion() {
        addStaticEffect(new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Combustion on the battlefield?"
        ));
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
    }
}

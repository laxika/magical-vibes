package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;

@CardRegistration(set = "ODY", collectorNumber = "127")
public class DecayingSoil extends Card {

    public DecayingSoil() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileGraveyardCardsEffect(1, GraveyardExileScope.OWN));

        var threshold = new GraveyardCardThreshold(7, null);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                        new MayPayManaEffect("{1}", new ReturnTriggeringCardToOwnerHandEffect(),
                                "Pay {1} to return that card to your hand?"),
                        GrantScope.SELF)));
    }
}

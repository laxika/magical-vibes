package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyNextLoyaltyAbilityThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "738")
@CardRegistration(set = "CMM", collectorNumber = "768")
public class JayasPhoenix extends Card {

    public JayasPhoenix() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                new CopyNextLoyaltyAbilityThisTurnEffect());
        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.PLANESWALKER)), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Return Jaya's Phoenix from your graveyard to the battlefield?"));
    }
}

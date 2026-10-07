package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "16")
@CardRegistration(set = "MID", collectorNumber = "11")
@CardRegistration(set = "DBL", collectorNumber = "11")
public class CatharsCall extends Card {

    public CatharsCall() {
        // Enchant creature
        // Enchanted creature has vigilance and "At the beginning of your end step,
        // create a 1/1 white Human creature token."
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ENCHANTED_CREATURE))
                // The granted ability is the creature's own, so its controller gets the token
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new CreateTokenEffect("Human", 1, 1,
                                CardColor.WHITE, List.of(CardSubtype.HUMAN), Set.of(), Set.of()),
                        GrantScope.ENCHANTED_CREATURE));
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "13")
public class CommandersAuthority extends Card {

    public CommandersAuthority() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                        "Human", 1, 1, CardColor.WHITE, List.of(CardSubtype.HUMAN),
                        Set.<Keyword>of(), Set.<CardType>of()), GrantScope.ENCHANTED_CREATURE));
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithAttachedCountCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "72")
@CardRegistration(set = "PIP", collectorNumber = "397")
@CardRegistration(set = "PIP", collectorNumber = "600")
@CardRegistration(set = "PIP", collectorNumber = "925")
public class AnimalFriend extends Card {

    public AnimalFriend() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ATTACK,
                new CreateTokenWithAttachedCountCountersEffect(
                        new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SQUIRREL), Set.of(), Set.of()),
                        CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.ENCHANTED_CREATURE));
    }
}

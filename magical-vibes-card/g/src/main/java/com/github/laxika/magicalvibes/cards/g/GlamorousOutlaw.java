package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "190")
public class GlamorousOutlaw extends Card {

    public GlamorousOutlaw() {
        addCastingOption(new ExileCast());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        ActivatedAbility landManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(
                        new Fixed(1),
                        ManaSpendRestriction.NONE,
                        null,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Set.of(),
                        false,
                        List.of(ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED)
                )),
                "{T}: Add {U}, {B}, or {R}."
        );
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantActivatedAbilityEffect(
                        landManaAbility,
                        GrantScope.TARGET,
                        null,
                        EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE
                )),
                "{2}, Exile this card from your hand: Target land gains \"{T}: Add {U}, {B}, or {R}\" until this card is cast from exile.",
                TargetFilters.land()
        ).withExilesSourceFromHand());
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "10")
public class SecretArcadeDustyParlor extends Card {

    public SecretArcadeDustyParlor() {
        setRoomDoorManaCosts(List.of("{4}{W}", "{2}{W}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Secret Arcade", List.of())
                        .withManaCost("{4}{W}"),
                new ChooseOneEffect.ChooseOneOption("Dusty Parlor", List.of())
                        .withManaCost("{2}{W}")
        )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantCardTypeToOwnNonlandPermanentsEffect(CardType.ENCHANTMENT, false)));

        target(TargetFilters.creature(), 0, 1).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        List.of(new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                        null,
                        TargetFilters.creature(),
                        null,
                        false,
                        false,
                        new SourceRoomDoorUnlocked(1),
                        0));
    }
}

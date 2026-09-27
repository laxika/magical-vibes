package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "45")
@CardRegistration(set = "LTC", collectorNumber = "128")
public class WindswiftSlice extends Card {

    public WindswiftSlice() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, TargetDealsPowerDamageToTargetEffect.recordingExcessDamage())
                .addEffect(EffectSlot.SPELL, new CreateTokenEffect(new EventValue(), "Elf Warrior", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}

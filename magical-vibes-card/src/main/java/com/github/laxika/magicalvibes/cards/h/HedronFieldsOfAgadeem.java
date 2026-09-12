package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "OPC2", collectorNumber = "19")
public class HedronFieldsOfAgadeem extends Card {

    public HedronFieldsOfAgadeem() {
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentPowerAtLeastPredicate(7),
                "Creatures with power 7 or greater can't attack or block"));

        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE, 1, "Eldrazi", 7, 7, null, null,
                List.of(CardSubtype.ELDRAZI), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                        1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER)),
                List.of(), false, false, false, 0, Set.of()));
    }
}

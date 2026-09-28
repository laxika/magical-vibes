package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardAndBoostEquippedCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttached;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "165")
public class PactWeapon extends Card {

    public PactWeapon() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsAttached(), new CantLoseGameFromLifeEffect()));

        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new DrawCardEffect(1),
                new RevealDrawnCardAndBoostEquippedCreatureByManaValueEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, "a card"), new EquipEffect()),
                "Equip — Discard a card.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}

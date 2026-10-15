package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "138")
public class MaskOfIntolerance extends Card {

    public MaskOfIntolerance() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                controlsAtLeastFourBasicLandTypes(),
                new DealDamageToPlayersEffect(3, DamageRecipient.ACTIVE_PLAYER)));
    }

    private static Condition controlsAtLeastFourBasicLandTypes() {
        return new AnyOf(List.of(
                controlsFourBasicLandTypes(CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST),
                controlsFourBasicLandTypes(CardSubtype.PLAINS, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST),
                controlsFourBasicLandTypes(CardSubtype.PLAINS, CardSubtype.ISLAND,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST),
                controlsFourBasicLandTypes(CardSubtype.PLAINS, CardSubtype.ISLAND,
                        CardSubtype.SWAMP, CardSubtype.FOREST),
                controlsFourBasicLandTypes(CardSubtype.PLAINS, CardSubtype.ISLAND,
                        CardSubtype.SWAMP, CardSubtype.MOUNTAIN)));
    }

    private static Condition controlsFourBasicLandTypes(CardSubtype first, CardSubtype second,
                                                         CardSubtype third, CardSubtype fourth) {
        return new AllOf(List.of(
                controlsLandOfType(first),
                controlsLandOfType(second),
                controlsLandOfType(third),
                controlsLandOfType(fourth)));
    }

    private static Condition controlsLandOfType(CardSubtype subtype) {
        return new ActivePlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSubtypePredicate(subtype))));
    }
}

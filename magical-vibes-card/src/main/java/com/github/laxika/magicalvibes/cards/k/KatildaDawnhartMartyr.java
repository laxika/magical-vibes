package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "21")
public class KatildaDawnhartMartyr extends Card {

    public KatildaDawnhartMartyr() {
        setBackFaceCard(new KatildasRisingDawn());

        PermanentCount spiritsOrEnchantments = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        new PermanentIsEnchantmentPredicate())),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.VAMPIRE)));
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(spiritsOrEnchantments, spiritsOrEnchantments));
        addCastingOption(new DisturbCast("{3}{W}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "KatildasRisingDawn";
    }
}

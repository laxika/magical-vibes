package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "179")
public class AshayaSoulOfTheWild extends Card {

    public AshayaSoulOfTheWild() {
        PermanentCount landsYouControl =
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));

        PermanentNotPredicate nontoken = new PermanentNotPredicate(new PermanentIsTokenPredicate());
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.LAND, GrantScope.SELF, nontoken));
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.LAND, GrantScope.OWN_CREATURES, nontoken));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.FOREST, GrantScope.SELF, false, nontoken));
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.FOREST, GrantScope.OWN_CREATURES, false, nontoken));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.GREEN), GrantScope.ALL_OWN_CREATURES, nontoken));
    }
}

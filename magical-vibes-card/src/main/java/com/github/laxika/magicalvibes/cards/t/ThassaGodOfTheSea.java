package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "66")
public class ThassaGodOfTheSea extends Card {

    public ThassaGodOfTheSea() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.BLUE, 5)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{1}{U}: Target creature you control can't be blocked this turn.",
                TargetFilters.creatureYouControl()));
    }
}

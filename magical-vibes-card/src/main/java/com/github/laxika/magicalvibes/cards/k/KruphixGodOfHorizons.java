package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceManaDrainWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;

import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "152")
public class KruphixGodOfHorizons extends Card {

    public KruphixGodOfHorizons() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorsAtLeast(
                        Set.of(ManaColor.GREEN, ManaColor.BLUE), 7)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.STATIC, new ReplaceManaDrainWithColorlessEffect());
    }
}

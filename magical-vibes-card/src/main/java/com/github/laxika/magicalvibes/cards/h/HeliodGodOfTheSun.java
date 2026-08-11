package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "17")
public class HeliodGodOfTheSun extends Card {

    public HeliodGodOfTheSun() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.WHITE, 5)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}{W}",
                List.of(new CreateTokenEffect(
                        1, "Cleric", 2, 1, CardColor.WHITE,
                        List.of(CardSubtype.CLERIC), Set.of(), Set.of(CardType.ENCHANTMENT))),
                "{2}{W}{W}: Create a 2/1 white Cleric enchantment creature token."));
    }
}

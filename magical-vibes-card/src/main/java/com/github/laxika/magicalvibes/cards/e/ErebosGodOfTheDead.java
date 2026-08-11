package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "85")
public class ErebosGodOfTheDead extends Card {

    public ErebosGodOfTheDead() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new DevotionToColorAtLeast(ManaColor.BLACK, 5)),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new OpponentsCantGainLifeEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new PayLifeCost(2), new DrawCardEffect(1)),
                "{1}{B}, Pay 2 life: Draw a card."));
    }
}

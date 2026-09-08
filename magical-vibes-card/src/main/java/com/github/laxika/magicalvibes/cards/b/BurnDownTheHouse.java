package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "131")
public class BurnDownTheHouse extends Card {

    public BurnDownTheHouse() {
        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Burn Down the House deals 5 damage to each creature and each planeswalker",
                        new MassDamageEffect(5, false, false, true, null)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create three 1/1 red Devil creature tokens with haste",
                        new CreateTokenEffect(
                                CardType.CREATURE, 3, "Devil", 1, 1, CardColor.RED, null,
                                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), false, false,
                                tokenEffects, List.of(), false, false, false, 0,
                                Set.of(Keyword.HASTE))
        ))));
    }
}

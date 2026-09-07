package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "132")
public class ChocoComet extends Card {

    public ChocoComet() {
        Map<EffectSlot, CardEffect> birdTokenEffects = Map.of(
                EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
        CreateTokenEffect birdToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Bird", 2, 2,
                CardColor.GREEN, null, List.of(CardSubtype.BIRD), Set.of(), Set.of(),
                false, false, birdTokenEffects, List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
        addEffect(EffectSlot.SPELL, birdToken);
    }
}

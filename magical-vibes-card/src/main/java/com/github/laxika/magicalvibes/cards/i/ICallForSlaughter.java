package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "337")
public class ICallForSlaughter extends Card {

    public ICallForSlaughter() {
        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 3, "Devil", 1, 1, CardColor.RED, null,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), false, false,
                tokenEffects, List.of(), false, false, false, 0, Set.of(Keyword.HASTE)));
        addEffect(EffectSlot.SPELL, new BoostControllerDamageThisTurnEffect(1));
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndStepEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STH", collectorNumber = "136")
public class HornetCannon extends Card {

    public HornetCannon() {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_ENTER_BATTLEFIELD, new DestroySelfAtEndStepEffect());
        CreateTokenEffect hornetToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Hornet", 1, 1,
                null, null, List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING, Keyword.HASTE), Set.of(CardType.ARTIFACT),
                false, false, tokenEffects, List.of(), false, false, false, 0, Set.of());

        addActivatedAbility(new ActivatedAbility(
                true, "{3}", List.of(hornetToken),
                "{3}, {T}: Create a 1/1 colorless Insect artifact creature token with flying and haste named Hornet. Destroy it at the beginning of the next end step."));
    }
}

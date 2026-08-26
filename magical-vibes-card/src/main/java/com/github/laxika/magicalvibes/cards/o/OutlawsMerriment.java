package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "198")
public class OutlawsMerriment extends Card {

    public OutlawsMerriment() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenAtRandomEffect(List.of(
                token("Human Warrior", 3, 1, List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR),
                        Set.of(Keyword.TRAMPLE, Keyword.HASTE), Map.of()),
                token("Human Cleric", 2, 1, List.of(CardSubtype.HUMAN, CardSubtype.CLERIC),
                        Set.of(Keyword.LIFELINK, Keyword.HASTE), Map.of()),
                token("Human Rogue", 1, 2, List.of(CardSubtype.HUMAN, CardSubtype.ROGUE),
                        Set.of(Keyword.HASTE),
                        Map.of(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(1))))));
    }

    private static CreateTokenEffect token(String name, int power, int toughness,
                                           List<CardSubtype> subtypes, Set<Keyword> keywords,
                                           Map<EffectSlot, CardEffect> tokenEffects) {
        return new CreateTokenEffect(
                CardType.CREATURE,
                1,
                name,
                power,
                toughness,
                null,
                Set.of(CardColor.RED, CardColor.WHITE),
                subtypes,
                keywords,
                Set.of(),
                false,
                false,
                tokenEffects,
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
    }
}

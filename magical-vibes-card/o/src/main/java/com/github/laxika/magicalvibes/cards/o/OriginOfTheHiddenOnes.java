package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackTokenCreationEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "36")
public class OriginOfTheHiddenOnes extends Card {

    public OriginOfTheHiddenOnes() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                2, "Assassin", 1, 1, CardColor.BLACK, List.of(CardSubtype.ASSASSIN),
                Set.of(Keyword.MENACE), Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new RegisterDelayedAttackTokenCreationEffect(
                1,
                new CreateTokenEffect(
                        1, "Assassin", 1, 1, CardColor.BLACK, List.of(CardSubtype.ASSASSIN),
                        Set.of(Keyword.MENACE), true, false),
                false,
                new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)));
    }
}

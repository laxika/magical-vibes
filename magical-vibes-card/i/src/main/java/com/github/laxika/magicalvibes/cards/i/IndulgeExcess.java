package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.Excess;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackTokenCreationEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Indulge // Excess — front half (Indulge).
 * Sorcery — Whenever a creature you control attacks this turn, create a tapped and attacking
 * Citizen token.
 */
@CardRegistration(set = "NCC", collectorNumber = "46")
public class IndulgeExcess extends Card {

    public IndulgeExcess() {
        setBackFaceCard(new Excess());

        addEffect(EffectSlot.SPELL, new RegisterDelayedAttackTokenCreationEffect(
                1,
                new CreateTokenEffect(
                        CardType.CREATURE, 1, "Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN),
                        Set.of(), Set.of(), true, false, Map.of(), List.of(), false, false,
                        false, 0, Set.of()),
                false,
                new PermanentIsCreaturePredicate()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Excess";
    }
}

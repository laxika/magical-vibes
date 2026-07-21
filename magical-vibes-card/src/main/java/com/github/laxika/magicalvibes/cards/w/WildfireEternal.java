package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "109")
public class WildfireEternal extends Card {

    public WildfireEternal() {
        // Afflict 4 — whenever this creature becomes blocked, defending player loses 4 life
        // (once per becoming blocked, not per blocker).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new LoseLifeEffect(4, LoseLifeRecipient.DEFENDING_PLAYER));

        // Whenever this creature attacks and isn't blocked, you may cast an instant or sorcery
        // spell from your hand without paying its mana cost.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))));
    }
}

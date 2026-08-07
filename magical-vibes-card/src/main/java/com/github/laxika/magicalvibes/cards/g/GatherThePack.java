package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "178")
public class GatherThePack extends Card {

    public GatherThePack() {
        // Reveal the top five cards of your library. You may put a creature card from among them
        // into your hand. Put the rest into your graveyard.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // put up to two creature cards from among the revealed cards into your hand instead of one.
        GraveyardCardThreshold spellMastery = new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY))));
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealUpToToHandRestToGraveyard(
                5, new CardTypePredicate(CardType.CREATURE), new FixedIfCondition(spellMastery, 2, 1)));
    }
}

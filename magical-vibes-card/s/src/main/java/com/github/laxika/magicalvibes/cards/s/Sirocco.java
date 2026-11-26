package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "192")
public class Sirocco extends Card {

    public Sirocco() {
        // Each blue instant card is its own question; the cards nobody paid for are discarded together after the
        // last answer, one instruction (CR 608.2c).
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addSpellEffect(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()))
                .addSpellEffect(UnlessPaysEffect.forEachCard(
                        Subjects.revealedThisWay(new CardAllOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardTypePredicate(CardType.INSTANT)))),
                        Subjects.targetPlayerDeclaredElsewhere(), new PayLifeCost(4), DiscardCardsEffect::new));
    }
}

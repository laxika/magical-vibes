package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInRevealedCards;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "EVE", collectorNumber = "29")
public class SanityGrinding extends Card {

    public SanityGrinding() {
        // Chroma — reveal the top ten cards; target opponent mills a card for each blue mana symbol
        // among them; then bottom the revealed cards in any order.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(10)))
                .addSpellEffect(new MillEffect(new ColorManaSymbolsInRevealedCards(ManaColor.BLUE), Subjects.targetPlayer()))
                .addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()));
    }
}

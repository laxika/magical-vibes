package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "56")
public class SkillBorrower extends Card {

    public SkillBorrower() {
        // Play with the top card of your library revealed.
        addStaticEffect(new PlayWithTopCardRevealedEffect());
        // As long as the top card of your library is an artifact or creature card,
        // this creature has all activated abilities of that card.
        addStaticEffect(new GainActivatedAbilitiesOfExiledCardsEffect(Subjects.topmostCardInYourLibrary(null),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE))),
                false));
    }
}

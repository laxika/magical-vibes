package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "INV", collectorNumber = "91")
@CardRegistration(set = "VMA", collectorNumber = "103")
public class Addle extends Card {

    public Addle() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new ChooseColorAtResolutionEffect())
                .addSpellEffect(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()))
                .addSpellEffect(ChooseCardEffect.announced(
                        Subjects.revealedThisWay(new CardHasChosenSpellColorPredicate()),
                        "a card of that color to discard"))
                .addSpellEffect(new ClearChosenColorEffect())
                .addSpellEffect(new DiscardCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}

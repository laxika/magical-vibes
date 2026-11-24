package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowPlayExiledCostCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "9")
public class ChitinousCrawler extends Card {

    public ChitinousCrawler() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                ConjureCardsEffect.duplicateOf(
                        Subjects.targetCard(new CardTypePredicate(CardType.CREATURE),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                        ConjureDestination.intoGraveyard()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileNCardsFromGraveyardCost(
                                1, null, new CardIsPermanentPredicate(), false, true),
                        new AllowPlayExiledCostCardThisTurnEffect(true)),
                "Exile a permanent card from your graveyard: You may play it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED)
                .withRequiredGraveyardCards(
                        new CardIsPermanentPredicate(), 8, "permanent cards in your graveyard"));
    }
}

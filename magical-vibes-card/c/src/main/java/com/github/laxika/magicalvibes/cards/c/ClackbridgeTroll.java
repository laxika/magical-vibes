package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "84")
@CardRegistration(set = "ELD", collectorNumber = "353")
public class ClackbridgeTroll extends Card {

    public ClackbridgeTroll() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(3, "Goat", 0, 1, CardColor.WHITE,
                        List.of(CardSubtype.GOAT), Set.of(), Set.of())
        ));

        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                UnlessPaysEffect.mayPay(Subjects.eachOpponent(),
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                        SequenceEffect.of(new TapPermanentsEffect(Subjects.source()),
                                new GainLifeEffect(3, Subjects.you()), new DrawCardEffect(1)))
                        .withPrompt("Sacrifice a creature? If a player does, it becomes tapped, its controller gains 3 life"
                                + " and draws a card.")
                        .payingTogether());
    }
}

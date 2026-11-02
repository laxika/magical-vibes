package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "79")
public class Precognition extends Card {

    public Precognition() {
        // At the beginning of your upkeep, you may look at the top card of target opponent's
        // library. If you do, you may put that card on the bottom of that player's library.
        // The outer MayEffect is the "you may look" choice, made before the top card is named. Accepting
        // it resolves MAY_PUT_TOP_ON_BOTTOM, whose prompt names the card and offers the bottom placement.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.MAY_PUT_TOP_ON_BOTTOM),
                        "Look at the top card of target opponent's library?"));
    }
}

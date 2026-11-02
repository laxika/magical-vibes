package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastSinceBeginningOfLastTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "14")
public class ConcertKaboomist extends Card {

    public ConcertKaboomist() {
        addMorph("{R}");

        SpellsCastSinceBeginningOfLastTurn noncreatureSpells =
                new SpellsCastSinceBeginningOfLastTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        CountScope.CONTROLLER);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(noncreatureSpells, DamageRecipient.TARGET_PLAYER))
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new DealDamageToPlayersEffect(noncreatureSpells, DamageRecipient.TARGET_PLAYER));
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastSinceBeginningOfLastTurn;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

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
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(noncreatureSpells, DamageRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_TURNED_FACE_UP,
                        new DealDamageToPlayersEffect(noncreatureSpells, DamageRecipient.TARGET_PLAYER));
    }
}

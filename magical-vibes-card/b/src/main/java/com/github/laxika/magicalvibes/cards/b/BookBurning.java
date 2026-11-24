package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "JUD", collectorNumber = "80")
public class BookBurning extends Card {

    public BookBurning() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                .addSpellEffect(MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                        new DealDamageToPlayersEffect(6, Subjects.acceptedPlayers()),
                        "Have it deal 6 damage to you? If no one does, target player mills six cards.",
                        null, new MillEffect(6, Subjects.targetPlayer())));
    }
}

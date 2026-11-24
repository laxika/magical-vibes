package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TSB", collectorNumber = "56")
@CardRegistration(set = "JUD", collectorNumber = "82")
@CardRegistration(set = "HOP", collectorNumber = "50")
@CardRegistration(set = "PD2", collectorNumber = "21")
@CardRegistration(set = "DDK", collectorNumber = "66")
@CardRegistration(set = "A25", collectorNumber = "123")
public class Browbeat extends Card {

    public Browbeat() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        addSpellEffect(MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                new DealDamageToPlayersEffect(5, Subjects.acceptedPlayers()),
                "Have it deal 5 damage to you? If no one does, target player draws three cards.",
                null, new DrawCardEffect(3, Subjects.targetPlayer())));
    }
}

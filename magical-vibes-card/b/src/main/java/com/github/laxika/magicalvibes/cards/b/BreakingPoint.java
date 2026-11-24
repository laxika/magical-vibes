package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "JUD", collectorNumber = "81")
@CardRegistration(set = "DDK", collectorNumber = "67")
public class BreakingPoint extends Card {

    public BreakingPoint() {
        addSpellEffect(MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                new DealDamageToPlayersEffect(6, Subjects.acceptedPlayers()),
                "Have it deal 6 damage to you? If no one does, destroy all creatures.",
                null, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate(), true)));
    }
}

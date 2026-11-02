package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "115")
@CardRegistration(set = "DBL", collectorNumber = "382")
public class GrafReaver extends Card {

    public GrafReaver() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(), "Target must be a planeswalker."
        )).addTrigger(Triggers.exploits(), new DestroyTargetPermanentEffect());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}

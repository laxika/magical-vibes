package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C14", collectorNumber = "28")
@CardRegistration(set = "MKC", collectorNumber = "132")
@CardRegistration(set = "MIC", collectorNumber = "127")
@CardRegistration(set = "C19", collectorNumber = "125")
@CardRegistration(set = "SCD", collectorNumber = "98")
@CardRegistration(set = "E01", collectorNumber = "36")
public class OverseerOfTheDamned extends Card {

    public OverseerOfTheDamned() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target creature?"));
        addTrigger(Triggers.creatureDies(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        ObjectRelation.OPPONENT_CONTROLS),
                CreateTokenEffect.blackZombie(1).withTapped(true));
    }
}

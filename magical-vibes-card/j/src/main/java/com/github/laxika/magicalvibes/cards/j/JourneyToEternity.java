package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AtzalCaveOfEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "160")
public class JourneyToEternity extends Card {

    public JourneyToEternity() {
        setBackFaceCard(new AtzalCaveOfEternity());

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        SequenceEffect.of(new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true),
                                new ReturnSourceTransformedFromGraveyardEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "AtzalCaveOfEternity";
    }
}

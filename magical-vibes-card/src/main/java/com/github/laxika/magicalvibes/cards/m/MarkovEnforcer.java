package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOC", collectorNumber = "26")
@CardRegistration(set = "VOC", collectorNumber = "64")
public class MarkovEnforcer extends Card {

    public MarkovEnforcer() {
        // Whenever this creature or another Vampire you control enters, this creature fights up to
        // one target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringCardConditionalEffect(
                                new CardSubtypePredicate(CardSubtype.VAMPIRE),
                                new SourceFightsTargetCreatureEffect()));

        // Whenever a creature dealt damage by this creature this turn dies, create a Blood token.
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, CreateTokenEffect.ofBloodToken(1));
    }
}

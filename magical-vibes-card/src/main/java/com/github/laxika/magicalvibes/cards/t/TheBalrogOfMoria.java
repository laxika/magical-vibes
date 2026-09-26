package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LTC", collectorNumber = "46")
@CardRegistration(set = "LTC", collectorNumber = "129")
public class TheBalrogOfMoria extends Card {

    public TheBalrogOfMoria() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.ON_DEATH, new MayEffect(
                        new ExileSourceCardFromGraveyardThenEffect(new ExileTargetPermanentEffect()),
                        "Exile The Balrog of Moria?"));
        addCycling("{3}{R}");
        addEffect(EffectSlot.ON_SELF_CYCLED, CreateTokenEffect.ofTreasureToken(2));
    }
}

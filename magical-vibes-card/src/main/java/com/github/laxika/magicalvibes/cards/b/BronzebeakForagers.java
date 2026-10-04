package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "37")
@CardRegistration(set = "LCC", collectorNumber = "69")
public class BronzebeakForagers extends Card {

    public BronzebeakForagers() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{W}",
                List.of(
                        new PutTargetCardExiledWithSourceIntoOwnersGraveyardEffect(null, true),
                        new GainLifeEffect(new XValue())),
                "{X}{W}: Put target card with mana value X exiled with this creature into its owner's graveyard. You gain X life."
        ));
    }
}

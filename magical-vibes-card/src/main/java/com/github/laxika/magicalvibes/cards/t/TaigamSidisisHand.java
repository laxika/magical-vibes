package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "357")
public class TaigamSidisisHand extends Card {

    public TaigamSidisisHand() {
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(3, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new ExileXCardsFromGraveyardCost(),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))
                ),
                "{B}, {T}, Exile X cards from your graveyard: Target creature gets -X/-X until end of turn.",
                TargetFilters.creature()
        ));
    }
}

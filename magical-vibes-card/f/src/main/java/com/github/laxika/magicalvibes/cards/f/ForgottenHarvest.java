package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "114")
public class ForgottenHarvest extends Card {

    public ForgottenHarvest() {
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffect(),
                "Exile a land card from your graveyard?"));
    }
}

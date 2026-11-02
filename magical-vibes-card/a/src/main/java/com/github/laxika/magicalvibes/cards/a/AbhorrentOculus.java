package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "42")
public class AbhorrentOculus extends Card {

    public AbhorrentOculus() {
        addCastingCost(new ExileNCardsFromGraveyardCost(6, null));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), ManifestDreadEffect.forController());
    }
}

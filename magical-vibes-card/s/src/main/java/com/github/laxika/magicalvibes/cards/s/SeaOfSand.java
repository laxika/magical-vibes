package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealEachDrawEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "33")
@CardRegistration(set = "OPCA", collectorNumber = "68")
public class SeaOfSand extends Card {

    public SeaOfSand() {
        CardEffect landDraw = new GainLifeEffect(new Fixed(3), GainLifeRecipient.TRIGGERING_PLAYER);
        CardEffect nonlandDraw = new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_PLAYER);
        RevealEachDrawEffect drawEffect = new RevealEachDrawEffect(landDraw, nonlandDraw);

        addTrigger(Triggers.youDrawCard(), drawEffect);
        addTrigger(Triggers.opponentDrawsCard(), drawEffect);
        target(TargetFilters.permanent()).addTrigger(Triggers.chaosEnsues(), new PutTargetOnTopOfLibraryEffect());
    }
}

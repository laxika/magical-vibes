package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutControllerCardFromHandOnTopOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "48")
public class SurvivorOfTheUnseen extends Card {

    public SurvivorOfTheUnseen() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{2}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(2), new PutControllerCardFromHandOnTopOfLibraryEffect()),
                "{T}: Draw two cards, then put a card from your hand on top of your library."
        ));
    }
}

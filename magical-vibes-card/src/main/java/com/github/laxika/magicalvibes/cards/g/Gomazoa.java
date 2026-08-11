package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutSourceAndBlockingCreaturesOnTopOfLibraryEffect;
import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "46")
public class Gomazoa extends Card {

    public Gomazoa() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutSourceAndBlockingCreaturesOnTopOfLibraryEffect()),
                "{T}: Put this creature and each creature it's blocking on top of their owners' libraries, then those players shuffle."));
    }
}

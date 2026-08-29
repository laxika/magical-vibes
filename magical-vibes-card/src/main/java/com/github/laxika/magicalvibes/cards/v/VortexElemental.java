package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceAndBlockingCreaturesOnTopOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "56")
public class VortexElemental extends Card {

    public VortexElemental() {
        addActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new PutSourceAndBlockingCreaturesOnTopOfLibraryEffect(true)),
                "{U}: Put this creature and each creature blocking or blocked by it on top of their owners' libraries, then those players shuffle."));
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}{U}",
                List.of(new MustBlockSourceEffect(null)),
                "{3}{U}{U}: Target creature blocks this creature this turn if able."));
    }
}

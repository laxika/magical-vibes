package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "113")
public class OgreRecluse extends Card {

    public OgreRecluse() {
        // Whenever a player casts a spell, tap this creature.
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(null,
                        List.of(new TapPermanentsEffect(TapUntapScope.SELF))));
    }
}

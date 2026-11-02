package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealForManaAndPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceManaDrainWithColorlessEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "249")
@CardRegistration(set = "MOM", collectorNumber = "313")
@CardRegistration(set = "MOM", collectorNumber = "387")
public class OmnathLocusOfAll extends Card {

    public OmnathLocusOfAll() {
        addStaticEffect(new ReplaceManaDrainWithColorlessEffect(ManaColor.BLACK));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new LookAtTopCardMayRevealForManaAndPutIntoHandEffect());
    }
}

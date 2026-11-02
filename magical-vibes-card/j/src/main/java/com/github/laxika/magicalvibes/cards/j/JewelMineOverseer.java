package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "21")
public class JewelMineOverseer extends Card {

    public JewelMineOverseer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConjureCardNamedIntoLibraryEffect(
                "ELD", "141", 7,
                List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1)))));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "6ED", collectorNumber = "256")
@CardRegistration(set = "5ED", collectorNumber = "331")
@CardRegistration(set = "4ED", collectorNumber = "274")
public class ThicketBasilisk extends Card {

    public ThicketBasilisk() {
        // Whenever this creature blocks or becomes blocked by a non-Wall creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addEffect(EffectSlot.ON_BLOCK, new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false),
                TriggerMode.PER_BLOCKER);
    }
}

package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "95")
public class WanderwineProphets extends Card {

    public WanderwineProphets() {
        // Champion a Merfolk (sacrifice unless you exile another Merfolk you control; it returns when this leaves).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.MERFOLK));

        // Whenever this deals combat damage to a player, you may sacrifice a Merfolk. If you do, take an extra turn after this one.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                                new ControllerExtraTurnEffect(1),
                                "a Merfolk"),
                        "You may sacrifice a Merfolk. If you do, take an extra turn after this one."));
    }
}

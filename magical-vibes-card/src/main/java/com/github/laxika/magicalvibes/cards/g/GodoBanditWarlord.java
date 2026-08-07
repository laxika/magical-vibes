package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "169")
public class GodoBanditWarlord extends Card {

    public GodoBanditWarlord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD),
                        "Search your library for an Equipment card?"));

        // "Whenever Godo attacks for the first time each turn": every half is wrapped in
        // OncePerTurnTriggerEffect, which the ON_ATTACK collector unwraps at most once per turn per
        // permanent — so attacking again in the extra combat phase this grants doesn't loop. Godo is
        // not a Samurai, so untapping itself is a separate SELF scope from the Samurai sweep.
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(
                new UntapPermanentsEffect(TapUntapScope.SELF)));
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentHasSubtypePredicate(CardSubtype.SAMURAI))));
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(new AdditionalCombatPhaseEffect(1)));
    }
}

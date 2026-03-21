package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;

import java.util.List;
import java.util.Set;

/**
 * Song of Freyalise — {1}{G} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I, II — Until your next turn, creatures you control gain "{T}: Add one mana of any color."
 * III — Put a +1/+1 counter on each creature you control. Those creatures gain vigilance,
 *        trample, and indestructible until end of turn.
 */
@CardRegistration(set = "DOM", collectorNumber = "179")
public class SongOfFreyalise extends Card {

    public SongOfFreyalise() {
        // Chapter I: Until your next turn, creatures you control gain "{T}: Add one mana of any color."
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect()),
                        "{T}: Add one mana of any color."),
                GrantScope.OWN_CREATURES, null, EffectDuration.UNTIL_YOUR_NEXT_TURN
        ));

        // Chapter II: Same as chapter I
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect()),
                        "{T}: Add one mana of any color."),
                GrantScope.OWN_CREATURES, null, EffectDuration.UNTIL_YOUR_NEXT_TURN
        ));

        // Chapter III: Put a +1/+1 counter on each creature you control.
        // Those creatures gain vigilance, trample, and indestructible until end of turn.
        addEffect(EffectSlot.SAGA_CHAPTER_III, new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES
        ));
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "CHK", collectorNumber = "230")
public class NaturesWill extends Card {

    public NaturesWill() {
        // Whenever one or more creatures you control deal combat damage to a player, tap all lands
        // that player controls and untap all lands you control. The damaged player is bound as the
        // trigger's target, so TARGET_PLAYERS_PERMANENTS taps their lands; CONTROLLED untaps ours.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsLandPredicate()),
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()))));
    }
}

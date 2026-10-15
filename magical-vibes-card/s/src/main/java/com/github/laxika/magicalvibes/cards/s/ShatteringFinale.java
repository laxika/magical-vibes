package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSNC", collectorNumber = "8")
public class ShatteringFinale extends Card {

    public ShatteringFinale() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfCasualtyPaidEffect());
        addEffect(EffectSlot.SPELL, new CasualtyCost(1));

        PermanentPredicate opponentCreature = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL,
                        new PerpetuallyBoostTargetCreatureEffect(0, -3, opponentCreature));
    }
}

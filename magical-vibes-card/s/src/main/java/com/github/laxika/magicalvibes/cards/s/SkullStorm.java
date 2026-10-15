package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C18", collectorNumber = "18")
public class SkullStorm extends Card {

    public SkullStorm() {
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellForEachCommanderCastEffect(false));
        addEffect(EffectSlot.SPELL, EachPlayerSacrificesPermanentOrLosesLifeEffect.opponentsMustSacrifice(
                new PermanentIsCreaturePredicate(),
                new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                "a creature"));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentOrLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C18", collectorNumber = "18")
public class SkullStorm extends Card {

    public SkullStorm() {
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellForEachCommanderCastEffect(false));
        addSpellEffect(EachPlayerSacrificesPermanentOrLosesLifeEffect.opponentsMustSacrifice(
                new PermanentIsCreaturePredicate(),
                new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                "a creature"));
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "228")
public class SocialSnub extends Card {

    public SocialSnub() {
        addTrigger(Triggers.youCastThisSpell(), CopyThisSpellIfConditionEffect.whenCastWhile(
                new ControlsPermanent(new PermanentIsCreaturePredicate()), true));
        addSpellEffect(new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER));
        addSpellEffect(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addSpellEffect(new GainLifeEffect(1));
    }
}

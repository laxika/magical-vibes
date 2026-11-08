package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "228")
public class SocialSnub extends Card {

    public SocialSnub() {
        // "When you cast this spell while you control a creature": the event condition.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new ControlsPermanent(new PermanentIsCreaturePredicate())),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Social Snub?"));
        addSpellEffect(new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER));
        addSpellEffect(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addSpellEffect(new GainLifeEffect(1));
    }
}

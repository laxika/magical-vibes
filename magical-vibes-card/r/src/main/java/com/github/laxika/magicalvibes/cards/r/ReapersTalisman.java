package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "117")
public class ReapersTalisman extends Card {

    public ReapersTalisman() {
        addTrigger(Triggers.attacks(), TriggerMode.INDEPENDENT,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TRIGGERING_PERMANENT));
        addTrigger(Triggers.attacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                SequenceEffect.of(new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER), new GainLifeEffect(2)));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}

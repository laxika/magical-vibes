package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AFR", collectorNumber = "117")
public class ReapersTalisman extends Card {

    public ReapersTalisman() {
        addEffect(EffectSlot.ON_ATTACK,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TRIGGERING_PERMANENT),
                TriggerMode.INDEPENDENT);
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(),
                SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER),
                        new GainLifeEffect(2))), TriggerMode.INDEPENDENT);
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}

package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetMaximumLifeTotalEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "320")
@CardRegistration(set = "MB2", collectorNumber = "556")
public class YouCompleatMe extends Card {

    private static final String EMBLEM_TEXT =
            "Pay 2 life: Add one mana of any color. At the beginning of your upkeep, "
                    + "you draw a card and you lose 1 life.";

    public YouCompleatMe() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControllerLifeAtLeast(11), new SetLifeTotalEffect(10)));
        addEffect(EffectSlot.SPELL, new SetMaximumLifeTotalEffect(10));
        addEffect(EffectSlot.SPELL, new CreateEmblemEffect(
                List.of(new EmblemStepTriggerEffect(
                        EmblemTriggerStep.UPKEEP,
                        List.of(SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                        "At the beginning of your upkeep, you draw a card and you lose 1 life.")),
                EMBLEM_TEXT,
                EmblemRecipient.CONTROLLER,
                List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new PayLifeCost(2), new AwardAnyColorManaEffect()),
                        "Pay 2 life: Add one mana of any color."))));
    }
}

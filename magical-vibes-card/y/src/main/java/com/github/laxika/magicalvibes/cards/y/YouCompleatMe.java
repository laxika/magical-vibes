package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetMaximumLifeTotalEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "320")
@CardRegistration(set = "MB2", collectorNumber = "556")
public class YouCompleatMe extends Card {

    private static final String EMBLEM_TEXT =
            "Pay 2 life: Add one mana of any color. At the beginning of your upkeep, "
                    + "you draw a card and you lose 1 life.";

    public YouCompleatMe() {
        addSpellEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(11), new SetLifeTotalEffect(10)));
        addSpellEffect(new SetMaximumLifeTotalEffect(10));
        addSpellEffect(new CreateEmblemEffect(
                List.of(),
                EMBLEM_TEXT,
                EmblemRecipient.CONTROLLER,
                List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new PayLifeCost(2), new AwardAnyColorManaEffect()),
                        "Pay 2 life: Add one mana of any color.")),
                List.of(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))))));
    }
}

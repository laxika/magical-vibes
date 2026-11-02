package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForNonlandCardsMilledEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "49")
@CardRegistration(set = "PIP", collectorNumber = "332")
@CardRegistration(set = "PIP", collectorNumber = "577")
@CardRegistration(set = "PIP", collectorNumber = "860")
public class ScreechingScorchbeast extends Card {

    public ScreechingScorchbeast() {
        addTrigger(Triggers.attacks(), new GiveEachPlayerRadCounterEffect(2));

        CreateTokenEffect zombieMutant = new CreateTokenEffect(
                new EventValue(), "Zombie Mutant", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT), Set.of(), Set.of());
        addTrigger(new TriggerCondition(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(
                        new CreateTokensForNonlandCardsMilledEffect(zombieMutant),
                        "Create that many Zombie Mutant tokens?"));
    }
}

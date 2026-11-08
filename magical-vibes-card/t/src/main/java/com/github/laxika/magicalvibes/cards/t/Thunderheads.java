package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "37")
@CardRegistration(set = "DDJ", collectorNumber = "23")
@CardRegistration(set = "GK1", collectorNumber = "27")
public class Thunderheads extends Card {

    public Thunderheads() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{2}{U}")));
        addSpellEffect(new CreateTokenEffect(
                CardType.CREATURE, 1, "Weird", 3, 3, CardColor.BLUE, null,
                List.of(CardSubtype.WEIRD), Set.of(Keyword.DEFENDER, Keyword.FLYING), Set.of(),
                false, false, List.of(), List.of(), false, true, false, 0, Set.of()));
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{2}{U}")),
                new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount("{2}{U}")));
    }
}

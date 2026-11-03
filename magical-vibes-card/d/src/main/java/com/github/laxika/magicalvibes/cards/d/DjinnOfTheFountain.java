package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "47")
public class DjinnOfTheFountain extends Card {

    public DjinnOfTheFountain() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Djinn of the Fountain gets +1/+1 until end of turn",
                                new BoostSelfEffect(1, 1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile Djinn of the Fountain. Return it to the battlefield under its owner's control at the beginning of the next end step",
                                FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false)),
                        new ChooseOneEffect.ChooseOneOption("Scry 1", new ScryEffect(1))
                )));
    }
}

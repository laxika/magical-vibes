package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "46")
public class EmergentHaunting extends Card {

    public EmergentHaunting() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true)),
                                new NotCondition(new SourceIsCreature())))),
                new AnimatePermanentsEffect(
                                3, 3,
                                List.of(CardSubtype.SPIRIT),
                                Set.of(Keyword.FLYING),
                                null,
                                Set.of(CardType.CREATURE),
                                Subjects.source(),
                                Duration.PERMANENT
                        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new SurveilEffect(1)),
                "{2}{U}: Surveil 1."
        ));
    }
}

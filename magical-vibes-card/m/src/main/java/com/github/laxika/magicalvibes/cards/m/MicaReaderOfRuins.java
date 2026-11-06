package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;

@CardRegistration(set = "SOS", collectorNumber = "124")
public class MicaReaderOfRuins extends Card {

    public MicaReaderOfRuins() {
        // Ward—Pay 3 life.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysLifeEffect(new Fixed(3)));

        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new MayEffect(
                        new SacrificePermanentThenEffect(new PermanentIsArtifactPredicate(), new CopyTriggeringSpellEffect(),
                                "an artifact", false, false),
                        "Sacrifice an artifact to copy that spell?"));
    }
}

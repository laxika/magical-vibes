package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "66")
public class ScribNibblers extends Card {

    public ScribNibblers() {
        // {T}: Exile the top card of target player's library. If it's a land card, you gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        ExileTopCardOfOwnLibraryEffect.of(Subjects.targetPlayer()),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.LAND)),
                                new GainLifeEffect(1))),
                "{T}: Exile the top card of target player's library. If it's a land card, you gain 1 life."
        ));

        // Landfall — Whenever a land you control enters, you may untap this creature.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayEffect(new UntapPermanentsEffect(Subjects.source()),
                        "Untap Scrib Nibblers?"));
    }
}

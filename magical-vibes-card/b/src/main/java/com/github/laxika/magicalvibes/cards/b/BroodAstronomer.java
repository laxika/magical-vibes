package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "18")
public class BroodAstronomer extends Card {

    /** The Planets Spellbook: Adagia, Evendo, Kavaron, Susur Secundi and Uthros. */
    private static final List<SpellbookCard> PLANETS = List.of(
            new SpellbookCard("EOE", "250"),
            new SpellbookCard("EOE", "253"),
            new SpellbookCard("EOE", "255"),
            new SpellbookCard("EOE", "259"),
            new SpellbookCard("EOE", "260"));

    public BroodAstronomer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                DraftFromSpellbookEffect.draftOntoBattlefield(PLANETS))),
                "Sacrifice a land?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new FixedIfCondition(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.PLANET),
                                new PermanentCounterCountAtLeastPredicate(CounterType.CHARGE, 12)
                        ))), 3, 1))),
                "{T}: Add one mana of any color. If you control a Planet with twelve or more charge counters on it, add three mana of any one color instead."
        ));
    }
}

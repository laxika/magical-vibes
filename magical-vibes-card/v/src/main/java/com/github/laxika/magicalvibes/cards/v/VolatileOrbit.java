package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "27")
public class VolatileOrbit extends Card {

    /** The Planets Spellbook: Adagia, Evendo, Kavaron, Susur Secundi and Uthros. */
    private static final List<SpellbookCard> PLANETS = List.of(
            new SpellbookCard("EOE", "250"),
            new SpellbookCard("EOE", "253"),
            new SpellbookCard("EOE", "255"),
            new SpellbookCard("EOE", "259"),
            new SpellbookCard("EOE", "260"));

    public VolatileOrbit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLANET))),
                DealDamageEffect.toAnyTarget(4));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        DraftFromSpellbookEffect.conjureOntoBattlefieldWithCounters(PLANETS, CounterType.CHARGE, 8)),
                "{1}{R}{G}, Sacrifice this enchantment: Conjure a card of your choice from the Planets Spellbook onto the battlefield tapped with eight charge counters on it. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}

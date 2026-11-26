package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlSinceLastUpkeep;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentGreaterThanSourceToughness;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * The triggered abilities that keywords stand for, which no card writes out. A permanent has a keyword's ability once
 * for each instance of the keyword it has as the layers leave it, each triggering separately (CR 702.100d, 702.108b,
 * 702.191b), and the trigger index lists them with its other triggered abilities, so they trigger, are counted by
 * "triggers an additional time" effects and are put on the stack as any other ability is.
 */
public final class KeywordTriggers {

    /** A keyword and the triggered ability it means. */
    public record KeywordTrigger(Keyword keyword, TriggeredAbility ability) {
    }

    /** Prowess: "Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn" (CR 702.108a). */
    public static final TriggeredAbility PROWESS = new TriggeredAbility(
            Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
            BoostEffect.self(1, 1));

    /**
     * Evolve: "Whenever a creature you control enters, if that creature's power is greater than this creature's power
     * and/or that creature's toughness is greater than this creature's toughness, put a +1/+1 counter on this
     * creature" (CR 702.100a).
     */
    public static final TriggeredAbility EVOLVE = new TriggeredAbility(
            Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
            TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentHasGreaterPowerOrToughnessThanSource()),
            new EvolveEffect());

    /**
     * Increment: "Whenever you cast a spell, if this permanent is a creature and the amount of mana spent to cast that
     * spell is greater than this creature's power or this creature's toughness, put a +1/+1 counter on this creature"
     * (CR 702.191a).
     */
    public static final TriggeredAbility INCREMENT = new TriggeredAbility(
            Triggers.youCastSpell().anySpell(),
            TriggerOptions.DEFAULT.withInterveningIf(new AllOf(List.of(new SourceIsCreature(),
                    new AnyOf(List.of(new SpellManaSpentGreaterThanSourcePower(),
                            new SpellManaSpentGreaterThanSourceToughness()))))),
            PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

    /**
     * Echo: "At the beginning of your upkeep, if this permanent came under your control since the beginning of your
     * last upkeep, sacrifice it unless you pay [cost]" (CR 702.30a), for the cost and paid effects {@code echo} names.
     * Its "if" is judged as it triggers and again as it resolves (CR 603.4); "unless you pay" is "you may pay; if you
     * don't, sacrifice it" (CR 118.12a). "When this creature's echo cost is paid, ..." is a triggered ability the
     * payment triggers, put on the stack after the echo ability resolves.
     */
    public static TriggeredAbility echo(EchoEffect echo) {
        List<CardEffect> paid = echo.paidEffects();
        CardEffect whenPaid = paid.isEmpty() ? null
                : new QueueReflexiveAbilityEffect(paid.size() == 1 ? paid.getFirst() : new SequenceEffect(paid));
        return new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new CameUnderControlSinceLastUpkeep()),
                new UnlessPaysEffect(Subjects.you(),
                        new PayEchoCost(echo.manaCost(), echo.handCardCost(), echo.cost(), echo.dynamicManaCost()),
                        new SacrificeSelfEffect(), whenPaid));
    }

    /**
     * Blitz's "When this permanent is put into a graveyard from the battlefield, draw a card" (CR 702.152a): a
     * permanent's own ability for as long as its blitz cost was paid, whether the blitz was printed or granted.
     */
    public static final TriggeredAbility BLITZ_DRAW = new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
            new DrawCardEffect(1));

    /** Every keyword that stands for a triggered ability the index lists, with that ability. */
    public static final List<KeywordTrigger> ALL = List.of(
            new KeywordTrigger(Keyword.PROWESS, PROWESS),
            new KeywordTrigger(Keyword.EVOLVE, EVOLVE),
            new KeywordTrigger(Keyword.INCREMENT, INCREMENT));

    private KeywordTriggers() {
    }
}

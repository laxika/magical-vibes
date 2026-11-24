package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Exchange control of this creature and that spell", "exchange control of target noncreature spell and target
 * creature": the permanent's controller gains control of the spell and the spell's controller gains control of the
 * permanent, indefinitely. Neither object changes zones. If either is gone (the permanent left the battlefield, the
 * spell left the stack, a target is illegal), or the permanent can't be controlled by the spell's controller, no part
 * of the exchange happens (CR 701.12a); if one player controls both, it does nothing (CR 701.12b, by its rulings). The
 * step records {@code StepKind.GAIN_CONTROL} (the permanent, its new controller) when the exchange happens, so "if you
 * do" is {@code HappenedThisWay(GAIN_CONTROL, 1)}. Choosing new targets for the spell is a following step,
 * {@code MayEffect(ChooseNewTargetsForTargetSpellEffect.of(spell), …)}.
 *
 * @param permanent {@code Subjects.source()} ("this creature", Perplexing Chimera) or {@code Subjects.target()}
 *                  ("target creature", which declares the benign creature target; Sudden Substitution)
 * @param spell     {@code Subjects.triggeringSpell()} ("that spell" of a spell-cast trigger) or
 *                  {@code Subjects.targetSpell()} ("target noncreature spell", declared by the card's target filter)
 */
public record ExchangeControlOfPermanentAndSpellEffect(PermanentSubject permanent, PermanentSubject spell)
        implements CardEffect {

    public ExchangeControlOfPermanentAndSpellEffect {
        Objects.requireNonNull(permanent, "permanent");
        if (!(permanent instanceof PermanentSubject.Source) && !(permanent instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Exchanges this permanent or a target creature: " + permanent);
        }
        if (!(spell instanceof PermanentSubject.TargetSpell) && !(spell instanceof PermanentSubject.TriggeringSpell)) {
            throw new IllegalArgumentException("Exchanges a target spell or the triggering spell: " + spell);
        }
    }

    /** "Exchange control of this creature and that spell" of a spell-cast trigger. */
    public static ExchangeControlOfPermanentAndSpellEffect sourceAndTriggeringSpell() {
        return new ExchangeControlOfPermanentAndSpellEffect(Subjects.source(), Subjects.triggeringSpell());
    }

    /** "Exchange control of target [noncreature] spell and target creature". */
    public static ExchangeControlOfPermanentAndSpellEffect targetCreatureAndTargetSpell() {
        return new ExchangeControlOfPermanentAndSpellEffect(Subjects.target(), Subjects.targetSpell());
    }

    @Override
    public TargetSpec targetSpec() {
        return permanent.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}

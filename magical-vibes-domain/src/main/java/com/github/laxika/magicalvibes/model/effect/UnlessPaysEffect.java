package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.Objects;

/**
 * "[Do something] unless [a player pays a cost]", which means "[that player] may pay [the cost]. If they don't, [do
 * something]" (CR 118.12a). The payment is a cost paid as the effect resolves, and "if they do / don't" checks the
 * choice to pay, not what happened (CR 118.12). The payer is asked even when the effect can do nothing (a spell that
 * can't be countered): the payment can still be made. A payer who couldn't pay the cost in full (CR 118.3; a player who
 * can't lose life can't pay life, CR 119.8) isn't asked, and {@code ifNotPaid} happens (CR 608.2d).
 *
 * <p>The chosen branch resolves in place of this effect, as a step of the same resolution (CR 608.2c): "that player"
 * after it is {@code Subjects.controllerOf(Subjects.targetSpell())}, the controller the spell had as it was countered
 * (CR 608.2h).
 *
 * <p>Ransoms: {@link PayManaCost} (a fixed or exact cost, {@code withGenericIncrease} for "{1} for each …", {@link
 * PayManaCost#x()} for the announced X, {@code lifeAmount} for "{2} and 2 life") and {@link PayLifeCost}. The factories
 * read as the Oracle text: {@code counterUnlessPays(3)} is "counter target spell unless its controller pays {3}" and
 * ward {3}.
 *
 * @param payer     who may pay ("its controller", "that player")
 * @param ransom    the cost they may pay
 * @param ifNotPaid what happens unless they pay
 * @param ifPaid    what happens when they pay, or {@code null} for nothing
 */
public record UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid)
        implements CardEffect {

    public UnlessPaysEffect {
        Objects.requireNonNull(payer, "payer");
        Objects.requireNonNull(ransom, "ransom");
        Objects.requireNonNull(ifNotPaid, "ifNotPaid");
        if (!(ransom instanceof PayManaCost) && !(ransom instanceof PayLifeCost)) {
            throw new IllegalArgumentException("A ransom is a mana or life payment: " + ransom);
        }
    }

    /** "Counter target spell (or ability) unless its controller pays {@code {amount}}"; ward {N}. */
    public static UnlessPaysEffect counterUnlessPays(int amount) {
        return counter(new PayManaCost("{" + amount + "}"));
    }

    /** "… unless its controller pays {@code manaCost}", an exact cost with coloured or hybrid symbols ({W}, {3/B}). */
    public static UnlessPaysEffect counterUnlessPays(String manaCost) {
        return counter(new PayManaCost(manaCost));
    }

    /** "… unless its controller pays {1} for each …" / "pays {X}, where X is …", the amount read as it resolves. */
    public static UnlessPaysEffect counterUnlessPays(DynamicAmount amount) {
        return counter(PayManaCost.withGenericIncrease("{0}", amount));
    }

    /** "… unless its controller pays {@code {mana}} and {@code life} life"; "Ward—{2}, Pay 2 life". */
    public static UnlessPaysEffect counterUnlessPays(int mana, int life) {
        return counter(new PayManaCost("{" + mana + "}", null, false, life));
    }

    /** "… unless its controller pays {X}", X announced as this spell or ability was put on the stack. */
    public static UnlessPaysEffect counterUnlessPaysX() {
        return counter(PayManaCost.x());
    }

    /** "… unless that player pays {@code life} life"; "Ward—Pay N life". */
    public static UnlessPaysEffect counterUnlessPaysLife(int life) {
        return counterUnlessPaysLife(new Fixed(life));
    }

    /** "… unless that player pays life equal to …" ("Ward—Pay life equal to this creature's power"). */
    public static UnlessPaysEffect counterUnlessPaysLife(DynamicAmount life) {
        return counter(new PayLifeCost(life));
    }

    private static UnlessPaysEffect counter(CostEffect ransom) {
        return new UnlessPaysEffect(Subjects.controllerOf(Subjects.targetSpell()), ransom, new CounterSpellEffect(), null);
    }

    /** "If that spell is countered this way, exile it instead of putting it into its owner's graveyard." */
    public UnlessPaysEffect exilingIfCountered() {
        if (!(ifNotPaid instanceof CounterSpellEffect counter)) {
            throw new IllegalStateException("Only a counter exiles what it counters: " + ifNotPaid);
        }
        return new UnlessPaysEffect(payer, ransom,
                new CounterSpellEffect(CounteredSpellDestination.EXILE, counter.spell()), ifPaid);
    }

    /** "If they do / If that player pays, [effect]." */
    public UnlessPaysEffect ifPaid(CardEffect effect) {
        return new UnlessPaysEffect(payer, ransom, ifNotPaid, Objects.requireNonNull(effect, "effect"));
    }

    /**
     * Whether {@code ifNotPaid} counters a spell or ability, alone or as its first step (Power Sink): the prompt and log
     * speak of countering, and the AI reads it as a counterspell.
     */
    public boolean counters() {
        return ifNotPaid instanceof CounterSpellEffect
                || ifNotPaid instanceof SequenceEffect sequence && !sequence.steps().isEmpty()
                && sequence.steps().getFirst() instanceof CounterSpellEffect;
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec spec = ifNotPaid.targetSpec();
        return spec.equals(TargetSpec.NONE) && ifPaid != null ? ifPaid.targetSpec() : spec;
    }
}

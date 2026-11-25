package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
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
 * PayManaCost#x()} for the announced X, {@code lifeAmount} for "{2} and 2 life"), {@link PayLifeCost}, a discard
 * ({@link DiscardCardTypeCost} of one card, {@link DiscardRandomCardCost}, {@link DiscardHandCost}), a sacrifice
 * ({@link SacrificePermanentCost}, {@link SacrificeMultiplePermanentsCost}), a blight
 * ({@link PutCounterOnControlledCreatureCost#blight}), {@link CollectEvidenceCost}, {@link ExileGraveyardCost},
 * {@link GetPoisonCountersCost}, {@link DamageFromSourceCost}, {@link WaterbendCost}, and {@link AnyOfCost} over
 * them ("discard a card or pay {2}"). The factories read as the Oracle text: {@code counterUnlessPays(3)} is "counter
 * target spell unless its controller pays {3}" and ward {3}.
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
        if (ransom instanceof AnyOfCost anyOf) {
            anyOf.alternatives().forEach(UnlessPaysEffect::requirePayableAtResolution);
        } else {
            requirePayableAtResolution(ransom);
        }
    }

    private static void requirePayableAtResolution(CostEffect ransom) {
        boolean payable = switch (ransom) {
            case PayManaCost ignored -> true;
            case PayLifeCost ignored -> true;
            case DiscardCardTypeCost discard -> discard.count() == 1 && !discard.manaValueEqualsX()
                    && !discard.sameName() && !discard.trackManaValue() && !discard.imprintOnSource();
            case DiscardRandomCardCost ignored -> true;
            case DiscardHandCost ignored -> true;
            case SacrificePermanentCost ignored -> true;
            case SacrificeMultiplePermanentsCost ignored -> true;
            case PutCounterOnControlledCreatureCost counters ->
                    counters.counterType() == com.github.laxika.magicalvibes.model.CounterType.MINUS_ONE_MINUS_ONE
                            && counters.amount() instanceof Fixed && !counters.optional();
            case CollectEvidenceCost evidence -> evidence.triggersCollectEvidence() && evidence.cardFilter() == null
                    && evidence.manaSymbolColor() == null && !evidence.usesTargetManaValue() && !evidence.optional();
            case ExileGraveyardCost ignored -> true;
            case GetPoisonCountersCost ignored -> true;
            case DamageFromSourceCost ignored -> true;
            case WaterbendCost waterbend -> !waterbend.scalesWithX() && !waterbend.optional();
            default -> false;
        };
        if (!payable) {
            throw new IllegalArgumentException("No resolution-time payment for this ransom: " + ransom);
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

    /** "… unless its controller discards a card"; "Ward—Discard a card". */
    public static UnlessPaysEffect counterUnlessDiscards() {
        return counter(new DiscardCardTypeCost(null, null));
    }

    /** "… unless its controller discards {@code label} card" ("Ward—Discard an enchantment, instant, or sorcery card"). */
    public static UnlessPaysEffect counterUnlessDiscards(CardPredicate filter, String label) {
        return counter(new DiscardCardTypeCost(Objects.requireNonNull(filter, "filter"), label));
    }

    /** "Ward—Discard a card at random." */
    public static UnlessPaysEffect counterUnlessDiscardsAtRandom() {
        return counter(new DiscardRandomCardCost(1));
    }

    /** "… unless its controller discards their hand" (payable with an empty hand: Perplex's ruling). */
    public static UnlessPaysEffect counterUnlessDiscardsHand() {
        return counter(new DiscardHandCost());
    }

    /** "… unless its controller discards a card or pays {@code {mana}}"; "Ward—Discard a card or pay {2}". */
    public static UnlessPaysEffect counterUnlessDiscardsOrPays(int mana) {
        return counter(AnyOfCost.of(new DiscardCardTypeCost(null, null), new PayManaCost("{" + mana + "}")));
    }

    /** "Ward—Sacrifice a {@code description}" ("a creature", "a Food"); "Ward—Sacrifice a permanent". */
    public static UnlessPaysEffect counterUnlessSacrifices(PermanentPredicate filter, String description) {
        return counter(new SacrificePermanentCost(filter, description, false));
    }

    /** "Ward—Sacrifice {@code count} {@code description}" ("three nonland permanents"), all together. */
    public static UnlessPaysEffect counterUnlessSacrifices(int count, PermanentPredicate filter, String description) {
        return counter(new SacrificeMultiplePermanentsCost(count, filter, description));
    }

    /**
     * "Counter that spell unless you sacrifice a {@code description}" of a cast trigger (Blood Funnel, Mana Vortex's
     * "counter it"): the triggering spell, not a target, and its payer is the trigger's controller.
     */
    public static UnlessPaysEffect counterThatSpellUnlessYouSacrifice(PermanentPredicate filter, String description) {
        return new UnlessPaysEffect(Subjects.you(), new SacrificePermanentCost(filter, description, false),
                CounterSpellEffect.triggeringSpell(), null);
    }

    /** "Ward—Blight {@code count}" (CR 701.68a). */
    public static UnlessPaysEffect counterUnlessBlights(int count) {
        return counter(PutCounterOnControlledCreatureCost.blight(count));
    }

    /** "Ward—Collect evidence {@code manaValue}" (CR 701.59a). */
    public static UnlessPaysEffect counterUnlessCollectsEvidence(int manaValue) {
        return counter(new CollectEvidenceCost(manaValue));
    }

    /** "… unless its controller exiles all cards from their graveyard" (Grip of Amnesia). */
    public static UnlessPaysEffect counterUnlessExilesGraveyard() {
        return counter(new ExileGraveyardCost());
    }

    /** "Ward—Get {@code count} poison counters". */
    public static UnlessPaysEffect counterUnlessGetsPoisonCounters(int count) {
        return counter(new GetPoisonCountersCost(count));
    }

    /** "… unless its controller has [this spell] deal {@code damage} damage to them" (Molten Influence). */
    public static UnlessPaysEffect counterUnlessTakesDamage(int damage) {
        return counter(new DamageFromSourceCost(new Fixed(damage)));
    }

    /** "Ward—Waterbend {@code {amount}}" (CR 701.67a). */
    public static UnlessPaysEffect counterUnlessWaterbends(int amount) {
        return counter(new WaterbendCost(amount));
    }

    /**
     * "Change the target of target spell with a single target unless that spell's controller pays {@code {amount}}"
     * (Divert); the new target is only another legal one (CR 115.7a).
     */
    public static UnlessPaysEffect changeTargetUnlessPays(int amount) {
        return new UnlessPaysEffect(Subjects.controllerOf(Subjects.targetSpell()), new PayManaCost("{" + amount + "}"),
                new ChangeTargetOfTargetSpellWithSingleTargetEffect(), null);
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

    /**
     * The spell or ability the counter of {@code ifNotPaid} acts on ({@code Subjects.targetSpell()}, or
     * {@code Subjects.triggeringSpell()} for a cast trigger's "counter that spell"), or {@code null} when it doesn't
     * counter.
     */
    public PermanentSubject counteredSpell() {
        if (ifNotPaid instanceof CounterSpellEffect counter) {
            return counter.spell();
        }
        if (ifNotPaid instanceof SequenceEffect sequence && !sequence.steps().isEmpty()
                && sequence.steps().getFirst() instanceof CounterSpellEffect counter) {
            return counter.spell();
        }
        return null;
    }

    /** Whether {@code ifNotPaid} changes the target of the target spell (Divert). */
    public boolean retargets() {
        return ifNotPaid instanceof ChangeTargetOfTargetSpellWithSingleTargetEffect;
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec spec = ifNotPaid.targetSpec();
        return spec.equals(TargetSpec.NONE) && ifPaid != null ? ifPaid.targetSpec() : spec;
    }
}

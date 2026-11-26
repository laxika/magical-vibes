package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
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
 * can't lose life can't pay life, CR 119.8) isn't asked, and {@code ifNotPaid} happens (CR 608.2d): only a payer
 * with no way at all to pay is left unasked (the resolution-time payment service's {@code couldPossiblyPay}); one
 * who might still make the mana is asked.
 *
 * <p>The same record is "[P] may pay [the cost]. If they do, [ifPaid]" ({@code ifNotPaid} {@code null}), and "[P]
 * may pay … If they do, … If they don't, …" (both set); "[P] may pay" means the same as "unless [P] pays" with the
 * branches the other way round (CR 118.12a). The factories read as the Oracle text: {@code youMayPay("{1}", draw)}
 * is "you may pay {1}. If you do, draw a card", {@code unlessYouPay("{U}", destroy)} is "destroy … unless you pay
 * {U}" and "you may pay {U}. If you don't, destroy …", {@code mayPay(triggeringPlayer(), "{2}", draw)} is "that
 * player may pay {2}. If they do, …".
 *
 * <p>The chosen branch resolves in place of this effect, as a step of the same resolution (CR 608.2c): "that player"
 * after it is {@code Subjects.controllerOf(Subjects.targetSpell())}, the controller the spell had as it was countered
 * (CR 608.2h).
 *
 * <p>Ransoms: {@link PayManaCost} (a fixed or exact cost, {@code withGenericIncrease} for "{1} for each …", {@link
 * PayManaCost#x()} for the announced X, {@link PayManaCost#ofTriggeringSpell()} for "that spell's mana cost",
 * {@code lifeAmount} for "{2} and 2 life"), {@link PayLifeCost}, a discard
 * ({@link DiscardCardTypeCost} of one or more cards, {@link DiscardRandomCardCost}, {@link DiscardHandCost}), a sacrifice
 * ({@link SacrificePermanentCost}, {@link SacrificeMultiplePermanentsCost}), a blight
 * ({@link PutCounterOnControlledCreatureCost#blight}), {@link CollectEvidenceCost}, {@link ExileGraveyardCost},
 * {@link GetPoisonCountersCost}, {@link DamageFromSourceCost}, {@link WaterbendCost}, {@link SacrificeSelfCost},
 * {@link ExileNCardsFromGraveyardCost}, {@link TapMultiplePermanentsCost} with a fixed count, {@link ExileCardsCost},
 * {@link SourceControllerDrawsCost}, {@link ReturnPermanentToHandCost}, {@link PutCardFromHandOnTopOfLibraryCost},
 * {@link LandDropCost}, {@link SacrificeCreaturesWithTotalPowerCost}, {@link PayEnergyCost} (an amount read as it
 * resolves, CR 608.2h), {@link ExileTopCardOfGraveyardCost}, {@link RemoveCounterFromSourceCost} of one kind,
 * {@link RemoveCounterFromControlledPermanentCost}, {@link PutCardsFromGraveyardOnBottomOfLibraryCost},
 * {@link ReturnCardFromGraveyardToHandCost}, {@link MillControllerCost}, {@link PutTypedCounterOnSourceCost},
 * {@link PayEchoCost} (echo's cost, paid as the cost it names as it resolves, with a "rather than pay the echo cost"
 * alternative as an any-of choice), {@link AnyOfCost} over them ("discard a card or pay {2}") and {@link AllOfCost} ("pay {1} and discard a card"). The
 * factories read as the Oracle text: {@code counterUnlessPays(3)} is "counter target spell unless its controller pays
 * {3}" and ward {3}.
 *
 * <p>A payer subject naming several players ("any player may pay", "unless an opponent pays", "any other player")
 * asks each of them, in APNAP order, every one even after another paid (CR 101.4); the branch is chosen once, after
 * the last answer: {@code ifPaid} if any of them paid, else {@code ifNotPaid} ({@code unlessPays(eachPlayer(), "{2}",
 * search)} is Rhystic Tutor).
 *
 * <p>"Each of them [does X] unless they pay" is a branch for each payer, decided by that payer's own choice
 * ({@link #eachUnless}): every payer chooses in APNAP order, then X happens to every one who didn't pay, at once
 * (CR 101.4). The players who chose to pay are the entry's accepted players
 * ({@code Subjects.acceptedPlayers()}) when the branch resolves.
 *
 * <p>A payer that is the effect's own target ("unless target opponent pays 3 life") declares that target when no
 * branch does ({@link #targetSpec()}).
 *
 * <p>The chosen branch is put next in the resolution and resolved like any other step, so a wrapper in it (an "if"
 * on the "if you do", a "you may" in "you may copy that spell unless …") is judged as at the top level.
 *
 * <p>{@link #payingTogether()}: "any player may sacrifice a creature of their choice. If a player does, …", "unless a
 * player has this deal 4 damage to them". Every player is asked in APNAP order, knowing the earlier answers; a player
 * choosing to pay picks the permanents right then (unasked when only that many could be chosen, CR 608.2d), and
 * nothing is paid until the last answer; then every chosen payment is made together, one event (CR 101.4, 608.2e: the
 * chosen permanents are sacrificed or tapped at the same time, the damage dealt at once), followed by the branch. A
 * chosen payment that can no longer be made then is skipped, and the branch still follows the choice (CR 118.12).
 * Without it, each player who chooses to pay pays at once, before the next player is asked.
 *
 * @param payer     who may pay ("its controller", "that player", "you")
 * @param ransom    the cost they may pay
 * @param ifNotPaid what happens unless they pay, or {@code null} for nothing
 * @param ifPaid    what happens when they pay, or {@code null} for nothing
 * @param prompt    the question asked, or {@code null} for the one the ransom reads ("Pay {1}?")
 * @param paidTogether whether every chosen payment is made together after the last answer ({@link #payingTogether()})
 */
public record UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid,
                               String prompt, boolean paidTogether)
        implements CardEffect {

    public UnlessPaysEffect {
        Objects.requireNonNull(payer, "payer");
        Objects.requireNonNull(ransom, "ransom");
        if (ifNotPaid == null && ifPaid == null) {
            throw new IllegalArgumentException("An \"unless\" or \"if you do\" needs an effect on one branch");
        }
        if (ransom instanceof AnyOfCost anyOf) {
            anyOf.alternatives().forEach(UnlessPaysEffect::requirePayableAtResolution);
        } else if (ransom instanceof AllOfCost allOf) {
            allOf.parts().forEach(UnlessPaysEffect::requirePayableAtResolution);
        } else {
            requirePayableAtResolution(ransom);
        }
        if (paidTogether && !payableTogether(ransom)) {
            throw new IllegalArgumentException("No payment made together for this ransom: " + ransom);
        }
    }

    public UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid) {
        this(payer, ransom, ifNotPaid, ifPaid, null, false);
    }

    public UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid,
                            String prompt) {
        this(payer, ransom, ifNotPaid, ifPaid, prompt, false);
    }

    /**
     * The ransoms a player can choose now and pay later with the others' ({@link #payingTogether()}): a sacrifice or a
     * tap of permanents picked as they answer, damage from the source, the source's controller drawing.
     */
    private static boolean payableTogether(CostEffect ransom) {
        return ransom instanceof SacrificePermanentCost || ransom instanceof SacrificeMultiplePermanentsCost
                || ransom instanceof TapMultiplePermanentsCost || ransom instanceof DamageFromSourceCost
                || ransom instanceof SourceControllerDrawsCost;
    }

    private static void requirePayableAtResolution(CostEffect ransom) {
        boolean payable = switch (ransom) {
            case PayManaCost ignored -> true;
            case PayLifeCost ignored -> true;
            case DiscardCardTypeCost discard -> !discard.manaValueEqualsX()
                    && !discard.sameName() && !discard.trackManaValue() && !discard.imprintOnSource();
            case DiscardRandomCardCost ignored -> true;
            case DiscardHandCost ignored -> true;
            case SacrificePermanentCost ignored -> true;
            case SacrificeMultiplePermanentsCost ignored -> true;
            case PutCounterOnControlledCreatureCost counters ->
                    counters.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                            && counters.amount() instanceof Fixed && !counters.optional();
            case CollectEvidenceCost evidence -> evidence.triggersCollectEvidence() && evidence.cardFilter() == null
                    && evidence.manaSymbolColor() == null && !evidence.usesTargetManaValue() && !evidence.optional();
            case ExileGraveyardCost ignored -> true;
            case GetPoisonCountersCost ignored -> true;
            case DamageFromSourceCost ignored -> true;
            case WaterbendCost waterbend -> !waterbend.scalesWithX() && !waterbend.optional();
            case SacrificeSelfCost sacrifice -> !sacrifice.trackPower() && !sacrifice.recordSacrificedPermanentSnapshot();
            case ExileNCardsFromGraveyardCost exile -> !exile.onlyFromGraveyard() && !exile.trackExiledCards();
            case TapMultiplePermanentsCost tap -> tap.count() instanceof Fixed && !tap.tracksChosenPermanents();
            case ExileCardsCost ignored -> true;
            case SourceControllerDrawsCost ignored -> true;
            case ReturnPermanentToHandCost ignored -> true;
            case PutCardFromHandOnTopOfLibraryCost ignored -> true;
            case LandDropCost ignored -> true;
            case SacrificeCreaturesWithTotalPowerCost ignored -> true;
            case PayEnergyCost ignored -> true;
            case ExileTopCardOfGraveyardCost ignored -> true;
            case RemoveCounterFromSourceCost counters -> counters.counterType() != CounterType.ANY;
            case RemoveCounterFromControlledPermanentCost ignored -> true;
            case PutCardsFromGraveyardOnBottomOfLibraryCost ignored -> true;
            case ReturnCardFromGraveyardToHandCost ignored -> true;
            case MillControllerCost ignored -> true;
            case PutTypedCounterOnSourceCost ignored -> true;
            case PayEchoCost echo -> echoPayable(echo);
            default -> false;
        };
        if (!payable) {
            throw new IllegalArgumentException("No resolution-time payment for this ransom: " + ransom);
        }
    }

    /** An echo cost is paid as the cost it names: a mana cost, an {X} read as it resolves, or another payable cost. */
    private static boolean echoPayable(PayEchoCost echo) {
        if (echo.handCardCost() != null) {
            requirePayableAtResolution(echo.handCardCost());
        } else if (echo.cost() != null) {
            requirePayableAtResolution(echo.cost());
        } else if (echo.dynamicEchoCost() == null && echo.echoCost() == null) {
            throw new IllegalArgumentException("An echo cost names a cost: " + echo);
        }
        return true;
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

    /** "You may pay {@code manaCost}. If you do, {@code ifYouDo}." */
    public static UnlessPaysEffect youMayPay(String manaCost, CardEffect ifYouDo) {
        return mayPay(Subjects.you(), manaCost, ifYouDo);
    }

    /** "You may pay {@code manaCost} and {@code life} life. If you do, {@code ifYouDo}" (both or neither). */
    public static UnlessPaysEffect youMayPay(String manaCost, int life, CardEffect ifYouDo) {
        return new UnlessPaysEffect(Subjects.you(), new PayManaCost(manaCost, null, false, life), null,
                Objects.requireNonNull(ifYouDo, "ifYouDo"));
    }

    /** "{@code effect} unless you pay {@code manaCost}"; "you may pay {@code manaCost}. If you don't, {@code effect}." */
    public static UnlessPaysEffect unlessYouPay(String manaCost, CardEffect effect) {
        return unlessPays(Subjects.you(), manaCost, effect);
    }

    /** "You may pay {@code life} life. If you do, {@code ifYouDo}." */
    public static UnlessPaysEffect youMayPayLife(int life, CardEffect ifYouDo) {
        return youMayPayLife(new Fixed(life), ifYouDo);
    }

    /** "You may pay life equal to {@code life}. If you do, {@code ifYouDo}", the amount read as it resolves. */
    public static UnlessPaysEffect youMayPayLife(DynamicAmount life, CardEffect ifYouDo) {
        return new UnlessPaysEffect(Subjects.you(), new PayLifeCost(life), null,
                Objects.requireNonNull(ifYouDo, "ifYouDo"));
    }

    /** "{@code effect} unless you pay {@code life} life"; "you may pay {@code life} life. If you don't, …". */
    public static UnlessPaysEffect unlessYouPayLife(int life, CardEffect effect) {
        return unlessPaysLife(Subjects.you(), life, effect);
    }

    /** "{@code payer} may pay {@code manaCost}. If they do, {@code ifTheyDo}" ("that player", "defending player"). */
    public static UnlessPaysEffect mayPay(PlayerSubject payer, String manaCost, CardEffect ifTheyDo) {
        return new UnlessPaysEffect(payer, new PayManaCost(manaCost), null,
                Objects.requireNonNull(ifTheyDo, "ifTheyDo"));
    }

    /** "{@code effect} unless {@code payer} pays {@code manaCost}"; "{@code payer} may pay … If they don't, …". */
    public static UnlessPaysEffect unlessPays(PlayerSubject payer, String manaCost, CardEffect effect) {
        return new UnlessPaysEffect(payer, new PayManaCost(manaCost), Objects.requireNonNull(effect, "effect"), null);
    }

    /**
     * "You may {@code cost}. If you do, {@code ifYouDo}" for a cost other than mana alone: "you may tap two untapped
     * creatures you control", "you may pay {1} and discard a card" ({@link AllOfCost}).
     */
    public static UnlessPaysEffect youMayPay(CostEffect cost, CardEffect ifYouDo) {
        return mayPay(Subjects.you(), cost, ifYouDo);
    }

    /** "{@code effect} unless you {@code cost}" ("sacrifice it unless you tap an untapped permanent you control"). */
    public static UnlessPaysEffect unlessYouPay(CostEffect cost, CardEffect effect) {
        return new UnlessPaysEffect(Subjects.you(), cost, Objects.requireNonNull(effect, "effect"), null);
    }

    /** "{@code payer} may {@code cost}. If they do, {@code ifTheyDo}" ("that player may discard a card at random"). */
    public static UnlessPaysEffect mayPay(PlayerSubject payer, CostEffect cost, CardEffect ifTheyDo) {
        return new UnlessPaysEffect(payer, cost, null, Objects.requireNonNull(ifTheyDo, "ifTheyDo"));
    }

    /**
     * "{@code effect} unless {@code payer} {@code cost}" for a cost other than mana alone ("unless a player has this deal
     * 4 damage to them", "unless any opponent sacrifices a creature"); "[P] may [cost]. If no one does, …".
     */
    public static UnlessPaysEffect unlessPays(PlayerSubject payer, CostEffect cost, CardEffect effect) {
        return new UnlessPaysEffect(payer, cost, Objects.requireNonNull(effect, "effect"), null);
    }

    /** "{@code effect} unless {@code payer} pays {@code life} life". */
    public static UnlessPaysEffect unlessPaysLife(PlayerSubject payer, int life, CardEffect effect) {
        return new UnlessPaysEffect(payer, new PayLifeCost(life), Objects.requireNonNull(effect, "effect"), null);
    }

    /**
     * "{@code payers} each [do X] unless they {@code ransom}" ("each opponent sacrifices a permanent unless they pay
     * {3}", "for each player, this deals 1 damage to that player unless they pay {B} or {3}"): each payer's own choice
     * decides what happens to them. Every payer chooses in APNAP order, then {@code effect} happens to the players who
     * didn't pay, together (CR 101.4): {@code effect.apply(payers)} when none paid, else
     * {@code effect.apply(except(payers, acceptedPlayers()))}, the players who chose to pay being the entry's
     * accepted players then.
     *
     * @param effect the branch over the players it happens to
     */
    public static UnlessPaysEffect eachUnless(PlayerSubject payers, CostEffect ransom,
                                              java.util.function.Function<PlayerSubject, CardEffect> effect) {
        return new UnlessPaysEffect(payers, ransom, Objects.requireNonNull(effect.apply(payers), "effect"),
                effect.apply(Subjects.except(payers, Subjects.acceptedPlayers())));
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
                new CounterSpellEffect(CounteredSpellDestination.EXILE, counter.spell()), ifPaid, prompt, paidTogether);
    }

    /** "If they do / If that player pays, [effect]." */
    public UnlessPaysEffect ifPaid(CardEffect effect) {
        return new UnlessPaysEffect(payer, ransom, ifNotPaid, Objects.requireNonNull(effect, "effect"), prompt,
                paidTogether);
    }

    /** "If they don't / If you don't, [effect]." */
    public UnlessPaysEffect ifNotPaid(CardEffect effect) {
        return new UnlessPaysEffect(payer, ransom, Objects.requireNonNull(effect, "effect"), ifPaid, prompt,
                paidTogether);
    }

    /** This payment asked with {@code question} rather than the question its ransom reads ("Pay {1}?"). */
    public UnlessPaysEffect withPrompt(String question) {
        return new UnlessPaysEffect(payer, ransom, ifNotPaid, ifPaid, Objects.requireNonNull(question, "question"),
                paidTogether);
    }

    /**
     * Every player's choice first, the payments together after the last answer (CR 101.4, 608.2e): "any player may
     * sacrifice a creature of their choice", "any player may have this deal 6 damage to them". The ransom is a
     * sacrifice, a tap, damage from the source or "has you draw a card".
     */
    public UnlessPaysEffect payingTogether() {
        return new UnlessPaysEffect(payer, ransom, ifNotPaid, ifPaid, prompt, true);
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

    /**
     * Whether {@code ifPaid} counters the spell that caused the trigger ("any other player may pay that spell's mana
     * cost. If a player does, counter the spell": Ice Cave): the prompt names that spell.
     */
    public boolean countersIfPaid() {
        return ifPaid instanceof CounterSpellEffect;
    }

    /** Whether {@code ifNotPaid} changes the target of the target spell (Divert). */
    public boolean retargets() {
        return ifNotPaid instanceof ChangeTargetOfTargetSpellWithSingleTargetEffect;
    }

    /**
     * The target of either branch: chosen as the ability is put on the stack (CR 603.3d), whichever branch the payment
     * then picks (CR 603.5); {@code ifNotPaid}'s first. When neither branch targets, the payer's own player target
     * ("unless target opponent pays 3 life"): asking a player to pay is against them, so it is harmful.
     */
    @Override
    public TargetSpec targetSpec() {
        TargetSpec spec = ifNotPaid == null ? TargetSpec.NONE : ifNotPaid.targetSpec();
        spec = spec.equals(TargetSpec.NONE) && ifPaid != null ? ifPaid.targetSpec() : spec;
        if (spec.equals(TargetSpec.NONE) && payer.declaresTarget()) {
            TargetSpec payerTarget = TargetSpec.harmful(TargetPredicates.player());
            return payer.targetChosenAtRandom() ? payerTarget.atRandom() : payerTarget;
        }
        return spec;
    }

    /** "You may pay {X}. When you do, [targeted effect]": the reflexive ability chooses the target (CR 603.12). */
    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return ifPaid != null && ifPaid.targetsChosenByReflexiveAbility();
    }
}

package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Exchanges control of the two permanents stored in {@code StackEntry.targetIds}: the first target
 * (a permanent the ability's controller controls) and the second target (a permanent an opponent
 * controls).
 *
 * <p>{@code targetPredicate} is the shape both targets must still match at resolution (nonland for
 * Puca's Mischief, land for Political Trickery); {@code firstTargetPredicate} can override that
 * shape for the first target when the two target positions have different restrictions;
 * {@code requireOpponentManaValueNotGreater} adds
 * Puca's Mischief's "with equal or lesser mana value" restriction on the second target.
 *
 * <p>{@code requireFirstTargetControlledByController} is {@code true} for cards whose wording pins
 * the first target to the ability's controller ("target land you control and target land an opponent
 * controls"). Cards that just say "two target creatures" (Switcheroo) pass {@code false}: either
 * target may be controlled by anyone, and the exchange happens whenever the two permanents have
 * different controllers.
 *
 * <p>Used by Puca's Mischief's upkeep trigger (target selection is mandatory at trigger time; the
 * "you may" is honoured at resolution by wrapping this effect in a {@link MayEffect}, see Axis of
 * Mortality) and by Political Trickery's and Switcheroo's two-target spells. At resolution the
 * exchange only happens if both targets are still legal, and only if the two permanents have
 * different controllers (CR 701.12a, CR 701.12b).
 *
 * <p>When {@code sourceIsFirstTarget} is set, the ability's own source permanent takes the place of
 * the first target and only a single target is declared — Conjured Currency's "exchange control of
 * this enchantment and target permanent you neither own nor control". In that mode the effect
 * declares a normal single-permanent {@code targetSpec()} narrowed by {@code targetPredicate}, so
 * the ordinary targeted-trigger machinery picks the target; {@code targetPredicate} is then only
 * checked against that one target, never against the source.
 *
 * <p>{@code requireSharedArtifactOrCreatureType} re-checks Legerdemain's cross-target "another
 * target permanent that shares one of those types with it" restriction at resolution; at
 * announcement it is enforced by {@code MultiTargetConstraint.SHARE_ARTIFACT_OR_CREATURE_TYPE}.
 * {@code requireSharedCardType} is the corresponding restriction for cards whose wording allows
 * any shared card type, such as Daring Thief.
 */
public record ExchangeControlOfTargetPermanentsEffect(
        PermanentPredicate targetPredicate,
        boolean requireOpponentManaValueNotGreater,
        boolean requireFirstTargetControlledByController,
        boolean sourceIsFirstTarget,
        boolean requireSharedArtifactOrCreatureType,
        boolean triggeringPermanentIsFirstTarget,
        boolean sacrificeSourceIfNoExchange,
        boolean requireSharedCardType,
        PermanentPredicate firstTargetPredicate,
        boolean targetPairInSingleGroup) implements CardEffect {

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater,
            boolean requireFirstTargetControlledByController, boolean sourceIsFirstTarget,
            boolean requireSharedArtifactOrCreatureType, boolean triggeringPermanentIsFirstTarget) {
        this(targetPredicate, requireOpponentManaValueNotGreater, requireFirstTargetControlledByController,
                sourceIsFirstTarget, requireSharedArtifactOrCreatureType, triggeringPermanentIsFirstTarget,
                false, false, null, false);
    }

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater) {
        this(targetPredicate, requireOpponentManaValueNotGreater, true, false, false, false,
                false, false, null, false);
    }

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater,
            boolean requireFirstTargetControlledByController) {
        this(targetPredicate, requireOpponentManaValueNotGreater,
                requireFirstTargetControlledByController, false, false, false, false, false, null, false);
    }

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater,
            boolean requireFirstTargetControlledByController, boolean sourceIsFirstTarget) {
        this(targetPredicate, requireOpponentManaValueNotGreater,
                requireFirstTargetControlledByController, sourceIsFirstTarget, false, false,
                false, false, null, false);
    }

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater,
            boolean requireFirstTargetControlledByController, boolean sourceIsFirstTarget,
            boolean requireSharedArtifactOrCreatureType) {
        this(targetPredicate, requireOpponentManaValueNotGreater,
                requireFirstTargetControlledByController, sourceIsFirstTarget,
                requireSharedArtifactOrCreatureType, false, false, false, null, false);
    }

    public static ExchangeControlOfTargetPermanentsEffect forTriggeringPermanent(
            PermanentPredicate targetPredicate) {
        return new ExchangeControlOfTargetPermanentsEffect(
                targetPredicate, false, false, false, false, true, false, false, null, false);
    }

    public static ExchangeControlOfTargetPermanentsEffect forControlledTargetsSharingCardType(
           PermanentPredicate targetPredicate) {
        return new ExchangeControlOfTargetPermanentsEffect(
                new PermanentTruePredicate(),
                false, true, false, false, false, false, true, targetPredicate, false);
    }

    public static ExchangeControlOfTargetPermanentsEffect forTriggeringPermanentAndSacrificeIfNoExchange(
            PermanentPredicate targetPredicate) {
        return new ExchangeControlOfTargetPermanentsEffect(
                targetPredicate, false, false, false, false, true, true, false, null, false);
    }

    /**
     * Modal exchange whose two targets are the two members of one target group.
     *
     * <p>This keeps the two targets distinct within a mode while allowing a permanent to be
     * chosen once for another selected mode.</p>
     */
    public static ExchangeControlOfTargetPermanentsEffect forTwoTargetsInOneGroup(
            PermanentPredicate targetPredicate) {
        return new ExchangeControlOfTargetPermanentsEffect(
                targetPredicate, false, false, false, false, false, false, false, null, true);
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return sacrificeSourceIfNoExchange;
    }

    @Override
    public TargetSpec targetSpec() {
        return sourceIsFirstTarget || triggeringPermanentIsFirstTarget
                ? TargetSpec.benign(TargetPredicates.permanent(), targetPredicate)
                : TargetSpec.NONE;
    }
}

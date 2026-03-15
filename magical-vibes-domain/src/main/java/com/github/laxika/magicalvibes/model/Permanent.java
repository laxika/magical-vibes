package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class Permanent {

    private final UUID id;
    private Card card;
    private final Card originalCard;
    private boolean tapped;
    private boolean attacking;
    /** The UUID of the player or planeswalker this creature is attacking. Null when not attacking. */
    @Setter private UUID attackTarget;
    private boolean attackedThisTurn;
    private boolean blocking;
    private final List<Integer> blockingTargets = new ArrayList<>();
    private final List<UUID> blockingTargetPermanentIds = new ArrayList<>();
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;
    @Setter private int damagePreventionShield;
    @Setter private int regenerationShield;
    @Setter private UUID attachedTo;
    @Setter private CardColor chosenColor;
    @Setter private String chosenName;
    @Setter private CardSubtype chosenSubtype;
    @Setter private boolean cantBeBlocked;
    @Setter private boolean cantBlockThisTurn;
    @Setter private boolean mustAttackThisTurn;
    /** When non-null, this creature must attack this specific player (not their planeswalkers). Cleared at end of turn. */
    @Setter private UUID mustAttackTargetId;
    @Setter private boolean cantRegenerateThisTurn;
    /** If true, this creature is exiled instead of dying this turn (e.g. Red Sun's Zenith). Cleared at end of turn. */
    @Setter private boolean exileInsteadOfDieThisTurn;
    /** If true, this creature has "Whenever this creature deals damage to an opponent, you may return target creature
     *  that player controls to its owner's hand" until end of turn (e.g. Arm with Aether). Cleared at end of turn. */
    @Setter private boolean hasDamageToOpponentCreatureBounce;
    @Setter private boolean powerToughnessSwitched;
    @Setter private boolean basePowerToughnessOverriddenUntilEndOfTurn;
    @Setter private int basePowerOverride;
    @Setter private int baseToughnessOverride;
    @Setter private boolean animatedUntilEndOfTurn;
    @Setter private int animatedPower;
    @Setter private int animatedToughness;
    @Setter private CardColor animatedColor;
    @Setter private boolean permanentlyAnimated;
    @Setter private int permanentAnimatedPower;
    @Setter private int permanentAnimatedToughness;
    @Setter private int loyaltyCounters;
    @Setter private int plusOnePlusOneCounters;
    @Setter private int minusOneMinusOneCounters;
    @Setter private int chargeCounters;
    @Setter private int phylacteryCounters;
    @Setter private int awakeningCounters;
    @Setter private int wishCounters;
    @Setter private boolean loyaltyAbilityUsedThisTurn;
    private final Set<Keyword> grantedKeywords = new HashSet<>();
    /** Keywords temporarily removed by one-shot effects (e.g. activated abilities).
     *  Cleared every turn by {@link #resetModifiers()}. */
    private final Set<Keyword> removedKeywords = new HashSet<>();
    /** Transient colors granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent color grants from one-shot effects, see {@link #grantedColors}. */
    private final Set<CardColor> transientColors = EnumSet.noneOf(CardColor.class);
    /** When {@code true}, {@link #transientColors} completely replaces the permanent's natural color
     *  (e.g. "target creature becomes blue"). When {@code false}, transient colors are additive. */
    @Setter private boolean colorOverridden;
    /** Transient subtypes granted by static/continuous effects (equipment, auras, animation, etc.).
     *  Cleared every turn by {@link #resetModifiers()} and recomputed from active static effect sources.
     *  For persistent subtype grants from one-shot effects, see {@link #grantedSubtypes}. */
    private final List<CardSubtype> transientSubtypes = new ArrayList<>();
    private final Set<CardType> grantedCardTypes = EnumSet.noneOf(CardType.class);
    private final List<TextReplacement> textReplacements = new ArrayList<>();
    private final Set<CardType> protectionFromCardTypes = EnumSet.noneOf(CardType.class);
    private final Set<CardColor> protectionFromColorsUntilEndOfTurn = EnumSet.noneOf(CardColor.class);
    private final Set<UUID> cantBlockIds = new HashSet<>();
    private final Set<UUID> mustBlockIds = new HashSet<>();
    /** If true, this permanent is exiled instead of going to any other zone when it leaves the battlefield (CR 614.6). */
    @Setter private boolean exileIfLeavesBattlefield;
    /** Source permanent IDs that prevent this permanent from untapping during its controller's untap step.
     *  Each entry means: "this permanent doesn't untap for as long as that source permanent remains tapped." */
    private final Set<UUID> untapPreventedByPermanentIds = new HashSet<>();
    /** Number of untap steps this permanent should skip. Decremented each untap step.
     *  Multiple triggers (e.g. land tapped twice while Vorinclex is out) stack independently.
     *  Used by Vorinclex, Voice of Hunger's opponent-land lock. */
    @Setter private int skipUntapCount;
    /** Accumulated damage marked on this creature (CR 704.5g). Reset during cleanup step. */
    @Setter private int markedDamage;
    /** Colors permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other colors").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient color grants from static effects, see {@link #transientColors}. */
    private final Set<CardColor> grantedColors = EnumSet.noneOf(CardColor.class);
    /** Subtypes permanently granted by one-shot effects (e.g. Rise from the Grave "in addition to its other types").
     *  NOT cleared by {@link #resetModifiers()} — survives turn resets.
     *  For transient subtype grants from static effects, see {@link #transientSubtypes}. */
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    @Setter private boolean transformed;

    public Permanent(Card card) {
        this.id = UUID.randomUUID();
        this.card = card;
        this.originalCard = card;
        this.tapped = false;
        this.attackedThisTurn = false;
        this.summoningSick = true;
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     * Preserves the same ID so that target references remain valid.
     * Card references are shared (immutable after construction).
     */
    public Permanent(Permanent source) {
        this.id = source.id;
        this.card = source.card;
        this.originalCard = source.originalCard;
        this.tapped = source.tapped;
        this.attacking = source.attacking;
        this.attackTarget = source.attackTarget;
        this.attackedThisTurn = source.attackedThisTurn;
        this.blocking = source.blocking;
        this.blockingTargets.addAll(source.blockingTargets);
        this.blockingTargetPermanentIds.addAll(source.blockingTargetPermanentIds);
        this.summoningSick = source.summoningSick;
        this.powerModifier = source.powerModifier;
        this.toughnessModifier = source.toughnessModifier;
        this.damagePreventionShield = source.damagePreventionShield;
        this.regenerationShield = source.regenerationShield;
        this.attachedTo = source.attachedTo;
        this.chosenColor = source.chosenColor;
        this.chosenName = source.chosenName;
        this.chosenSubtype = source.chosenSubtype;
        this.cantBeBlocked = source.cantBeBlocked;
        this.cantBlockThisTurn = source.cantBlockThisTurn;
        this.mustAttackThisTurn = source.mustAttackThisTurn;
        this.mustAttackTargetId = source.mustAttackTargetId;
        this.cantRegenerateThisTurn = source.cantRegenerateThisTurn;
        this.exileInsteadOfDieThisTurn = source.exileInsteadOfDieThisTurn;
        this.hasDamageToOpponentCreatureBounce = source.hasDamageToOpponentCreatureBounce;
        this.powerToughnessSwitched = source.powerToughnessSwitched;
        this.basePowerToughnessOverriddenUntilEndOfTurn = source.basePowerToughnessOverriddenUntilEndOfTurn;
        this.basePowerOverride = source.basePowerOverride;
        this.baseToughnessOverride = source.baseToughnessOverride;
        this.animatedUntilEndOfTurn = source.animatedUntilEndOfTurn;
        this.animatedPower = source.animatedPower;
        this.animatedToughness = source.animatedToughness;
        this.animatedColor = source.animatedColor;
        this.permanentlyAnimated = source.permanentlyAnimated;
        this.permanentAnimatedPower = source.permanentAnimatedPower;
        this.permanentAnimatedToughness = source.permanentAnimatedToughness;
        this.loyaltyCounters = source.loyaltyCounters;
        this.plusOnePlusOneCounters = source.plusOnePlusOneCounters;
        this.minusOneMinusOneCounters = source.minusOneMinusOneCounters;
        this.chargeCounters = source.chargeCounters;
        this.phylacteryCounters = source.phylacteryCounters;
        this.awakeningCounters = source.awakeningCounters;
        this.wishCounters = source.wishCounters;
        this.loyaltyAbilityUsedThisTurn = source.loyaltyAbilityUsedThisTurn;
        this.grantedKeywords.addAll(source.grantedKeywords);
        this.removedKeywords.addAll(source.removedKeywords);
        this.transientColors.addAll(source.transientColors);
        this.colorOverridden = source.colorOverridden;
        this.transientSubtypes.addAll(source.transientSubtypes);
        this.grantedCardTypes.addAll(source.grantedCardTypes);
        this.textReplacements.addAll(source.textReplacements);
        this.protectionFromCardTypes.addAll(source.protectionFromCardTypes);
        this.protectionFromColorsUntilEndOfTurn.addAll(source.protectionFromColorsUntilEndOfTurn);
        this.exileIfLeavesBattlefield = source.exileIfLeavesBattlefield;
        this.cantBlockIds.addAll(source.cantBlockIds);
        this.mustBlockIds.addAll(source.mustBlockIds);
        this.untapPreventedByPermanentIds.addAll(source.untapPreventedByPermanentIds);
        this.skipUntapCount = source.skipUntapCount;
        this.markedDamage = source.markedDamage;
        this.grantedColors.addAll(source.grantedColors);
        this.grantedSubtypes.addAll(source.grantedSubtypes);
        this.transformed = source.transformed;
    }

    public Card getOriginalCard() {
        return originalCard;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void tap() {
        this.tapped = true;
    }

    public void untap() {
        this.tapped = false;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
        if (attacking) {
            this.attackedThisTurn = true;
        }
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void addBlockingTarget(int blockingTarget) {
        this.blockingTargets.add(blockingTarget);
    }

    public void addBlockingTargetPermanentId(UUID permanentId) {
        this.blockingTargetPermanentIds.add(permanentId);
    }

    public void setSummoningSick(boolean summoningSick) {
        this.summoningSick = summoningSick;
    }

    public boolean isAttached() {
        return attachedTo != null;
    }

    public void clearCombatState() {
        this.attacking = false;
        this.attackTarget = null;
        this.blocking = false;
        this.blockingTargets.clear();
        this.blockingTargetPermanentIds.clear();
    }

    public void setAttackedThisTurn(boolean attackedThisTurn) {
        this.attackedThisTurn = attackedThisTurn;
    }

    public int getEffectivePower() {
        if (powerToughnessSwitched) {
            return getRawToughness();
        }
        return getRawPower();
    }

    public int getEffectiveToughness() {
        if (powerToughnessSwitched) {
            return getRawPower();
        }
        return getRawToughness();
    }

    private int getRawPower() {
        int basePower;
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            // Layer 7b: "set base P/T" with later timestamp overrides animation P/T
            basePower = basePowerOverride;
        } else if (animatedUntilEndOfTurn) {
            basePower = animatedPower;
        } else if (permanentlyAnimated) {
            basePower = permanentAnimatedPower;
        } else if (awakeningCounters > 0 && !card.hasType(CardType.CREATURE)) {
            basePower = 8;
        } else {
            basePower = card.getPower() != null ? card.getPower() : 0;
        }
        return basePower + powerModifier + plusOnePlusOneCounters - minusOneMinusOneCounters;
    }

    private int getRawToughness() {
        int baseToughness;
        if (basePowerToughnessOverriddenUntilEndOfTurn) {
            // Layer 7b: "set base P/T" with later timestamp overrides animation P/T
            baseToughness = baseToughnessOverride;
        } else if (animatedUntilEndOfTurn) {
            baseToughness = animatedToughness;
        } else if (permanentlyAnimated) {
            baseToughness = permanentAnimatedToughness;
        } else if (awakeningCounters > 0 && !card.hasType(CardType.CREATURE)) {
            baseToughness = 8;
        } else {
            baseToughness = card.getToughness() != null ? card.getToughness() : 0;
        }
        return baseToughness + toughnessModifier + plusOnePlusOneCounters - minusOneMinusOneCounters;
    }

    public CardColor getEffectiveColor() {
        if (colorOverridden && !transientColors.isEmpty()) {
            return transientColors.iterator().next();
        }
        if (animatedUntilEndOfTurn && animatedColor != null) {
            return animatedColor;
        }
        if (awakeningCounters > 0) {
            return CardColor.GREEN;
        }
        return card.getColor();
    }

    public boolean hasKeyword(Keyword keyword) {
        if (removedKeywords.contains(keyword)) return false;
        return card.getKeywords().contains(keyword) || grantedKeywords.contains(keyword);
    }

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
        this.powerToughnessSwitched = false;
        this.basePowerToughnessOverriddenUntilEndOfTurn = false;
        this.basePowerOverride = 0;
        this.baseToughnessOverride = 0;
        this.cantBeBlocked = false;
        this.cantBlockThisTurn = false;
        this.mustAttackThisTurn = false;
        this.mustAttackTargetId = null;
        this.cantRegenerateThisTurn = false;
        this.exileInsteadOfDieThisTurn = false;
        this.hasDamageToOpponentCreatureBounce = false;
        this.animatedUntilEndOfTurn = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.animatedColor = null;
        this.grantedKeywords.clear();
        this.removedKeywords.clear();
        this.transientColors.clear();
        this.colorOverridden = false;
        this.transientSubtypes.clear();
        this.grantedCardTypes.clear();
        this.protectionFromCardTypes.clear();
        this.protectionFromColorsUntilEndOfTurn.clear();
        this.cantBlockIds.clear();
        this.mustBlockIds.clear();
    }
}

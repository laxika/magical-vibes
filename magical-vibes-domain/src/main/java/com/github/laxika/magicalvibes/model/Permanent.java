package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
    private boolean attackedThisTurn;
    private boolean blocking;
    private final List<Integer> blockingTargets = new ArrayList<>();
    private boolean summoningSick;
    @Setter private int powerModifier;
    @Setter private int toughnessModifier;
    @Setter private int damagePreventionShield;
    @Setter private int regenerationShield;
    @Setter private UUID attachedTo;
    @Setter private CardColor chosenColor;
    @Setter private boolean cantBeBlocked;
    @Setter private boolean cantRegenerateThisTurn;
    @Setter private boolean animatedUntilEndOfTurn;
    @Setter private int animatedPower;
    @Setter private int animatedToughness;
    @Setter private int loyaltyCounters;
    @Setter private boolean loyaltyAbilityUsedThisTurn;
    private final Set<Keyword> grantedKeywords = new HashSet<>();
    private final List<CardSubtype> grantedSubtypes = new ArrayList<>();
    private final List<TextReplacement> textReplacements = new ArrayList<>();
    private final Set<UUID> cantBlockIds = new HashSet<>();

    public Permanent(Card card) {
        this.id = UUID.randomUUID();
        this.card = card;
        this.originalCard = card;
        this.tapped = false;
        this.attackedThisTurn = false;
        this.summoningSick = true;
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

    public void setSummoningSick(boolean summoningSick) {
        this.summoningSick = summoningSick;
    }

    public void clearCombatState() {
        this.attacking = false;
        this.blocking = false;
        this.blockingTargets.clear();
    }

    public void setAttackedThisTurn(boolean attackedThisTurn) {
        this.attackedThisTurn = attackedThisTurn;
    }

    public int getEffectivePower() {
        if (animatedUntilEndOfTurn) {
            return animatedPower + powerModifier;
        }
        return (card.getPower() != null ? card.getPower() : 0) + powerModifier;
    }

    public int getEffectiveToughness() {
        if (animatedUntilEndOfTurn) {
            return animatedToughness + toughnessModifier;
        }
        return (card.getToughness() != null ? card.getToughness() : 0) + toughnessModifier;
    }

    public boolean hasKeyword(Keyword keyword) {
        return card.getKeywords().contains(keyword) || grantedKeywords.contains(keyword);
    }

    public void resetModifiers() {
        this.powerModifier = 0;
        this.toughnessModifier = 0;
        this.cantBeBlocked = false;
        this.cantRegenerateThisTurn = false;
        this.animatedUntilEndOfTurn = false;
        this.animatedPower = 0;
        this.animatedToughness = 0;
        this.grantedKeywords.clear();
        this.grantedSubtypes.clear();
        this.cantBlockIds.clear();
    }
}

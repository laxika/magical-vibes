package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CreateTokenEffect(
        CardType primaryType,
        DynamicAmount amount,
        String tokenName,
        DynamicAmount power,
        DynamicAmount toughness,
        CardColor color,
        Set<CardColor> colors,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes,
        boolean tappedAndAttacking,
        boolean tapped,
        Map<EffectSlot, CardEffect> tokenEffects,
        List<ActivatedAbility> tokenAbilities,
        boolean exileAtEndOfCombat,
        boolean exileAtEndStep,
        boolean legendary,
        int initialPlusOnePlusOneCounters,
        Set<Keyword> grantedKeywordsUntilEndOfTurn
) implements TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return amount;
    }

    @Override
    public CardType tokenType() {
        return primaryType;
    }

    /** Printed power when fixed; dynamic power/toughness ("an X/X token") reports 0 to the AI estimators. */
    @Override
    public int tokenPower() {
        return power instanceof Fixed f ? f.value() : 0;
    }

    @Override
    public int tokenToughness() {
        return toughness instanceof Fixed f ? f.value() : 0;
    }

    /** Canonical shape with a fixed token count and printed power/toughness */
    public CreateTokenEffect(CardType primaryType, int amount, String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             boolean tappedAndAttacking, boolean tapped,
                             Map<EffectSlot, CardEffect> tokenEffects, List<ActivatedAbility> tokenAbilities,
                             boolean exileAtEndOfCombat, boolean exileAtEndStep, boolean legendary,
                             int initialPlusOnePlusOneCounters, Set<Keyword> grantedKeywordsUntilEndOfTurn) {
        this(primaryType, new Fixed(amount), tokenName, new Fixed(power), new Fixed(toughness), color, colors, subtypes, keywords, additionalTypes, tappedAndAttacking, tapped, tokenEffects, tokenAbilities, exileAtEndOfCombat, exileAtEndStep, legendary, initialPlusOnePlusOneCounters, grantedKeywordsUntilEndOfTurn);
    }

    /** Copy of this blueprint with a different (already-evaluated) token count, all other fields preserved. */
    public CreateTokenEffect withAmount(int newAmount) {
        return new CreateTokenEffect(primaryType, new Fixed(newAmount), tokenName, power, toughness, color, colors, subtypes, keywords, additionalTypes, tappedAndAttacking, tapped, tokenEffects, tokenAbilities, exileAtEndOfCombat, exileAtEndStep, legendary, initialPlusOnePlusOneCounters, grantedKeywordsUntilEndOfTurn);
    }

    /** Canonical shape with a dynamic token count and printed power/toughness */
    public CreateTokenEffect(CardType primaryType, DynamicAmount amount, String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             boolean tappedAndAttacking, boolean tapped,
                             Map<EffectSlot, CardEffect> tokenEffects, List<ActivatedAbility> tokenAbilities,
                             boolean exileAtEndOfCombat, boolean exileAtEndStep, boolean legendary,
                             int initialPlusOnePlusOneCounters, Set<Keyword> grantedKeywordsUntilEndOfTurn) {
        this(primaryType, amount, tokenName, new Fixed(power), new Fixed(toughness), color, colors, subtypes, keywords, additionalTypes, tappedAndAttacking, tapped, tokenEffects, tokenAbilities, exileAtEndOfCombat, exileAtEndStep, legendary, initialPlusOnePlusOneCounters, grantedKeywordsUntilEndOfTurn);
    }

    /** Single-color creature token (existing pattern) */
    public CreateTokenEffect(String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, 1, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Single-color creature token with amount */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Single-color creature token with a dynamically computed count ("for each …" wordings) */
    public CreateTokenEffect(DynamicAmount amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, amount, tokenName, new Fixed(power), new Fixed(toughness), color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Single creature token with dynamically computed power/toughness ("an X/X … token, where X is …") */
    public CreateTokenEffect(String tokenName, DynamicAmount power, DynamicAmount toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, new Fixed(1), tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Multi-color creature token */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors,
                             List<CardSubtype> subtypes) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Multi-color creature token with innate keywords and keywords granted until end of turn */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<Keyword> grantedKeywordsUntilEndOfTurn) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, colors, subtypes, keywords, Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, grantedKeywordsUntilEndOfTurn);
    }

    /** Multi-color creature token (single) */
    public CreateTokenEffect(String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors,
                             List<CardSubtype> subtypes) {
        this(CardType.CREATURE, 1, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Multi-color creature token with +1/+1 counters on creation */
    public CreateTokenEffect(String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors,
                             List<CardSubtype> subtypes, int initialPlusOnePlusOneCounters) {
        this(CardType.CREATURE, 1, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, initialPlusOnePlusOneCounters, Set.of());
    }

    /** Single-color creature token, tapped and attacking */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             boolean tappedAndAttacking) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, Set.of(), Set.of(), tappedAndAttacking, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Single-color creature token with keywords, tapped and attacking, exile at end of combat */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, boolean tappedAndAttacking,
                             boolean exileAtEndOfCombat) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, Set.of(), tappedAndAttacking, false, Map.of(), List.of(), exileAtEndOfCombat, false, false, 0, Set.of());
    }

    /** Single-color creature token with amount and token effects */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             Map<EffectSlot, CardEffect> tokenEffects) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, tokenEffects, List.of(), false, false, false, 0, Set.of());
    }

    /** Single-color creature token, enters tapped (not attacking) */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             boolean tapped) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, tapped, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Non-creature token with activated abilities (e.g. Treasure, Clue, Food) */
    public static CreateTokenEffect ofArtifactToken(int amount, String tokenName,
                                                     List<CardSubtype> subtypes,
                                                     List<ActivatedAbility> abilities) {
        return new CreateTokenEffect(CardType.ARTIFACT, amount, tokenName, 0, 0, null, null, subtypes, Set.of(), Set.of(), false, false, Map.of(), abilities, false, false, false, 0, Set.of());
    }

    /** 1/1 white Spirit creature token with flying */
    public static CreateTokenEffect whiteSpirit(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Spirit", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** 2/2 black Zombie creature token */
    public static CreateTokenEffect blackZombie(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Zombie", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** 1/1 white Soldier creature token */
    public static CreateTokenEffect whiteSoldier(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Soldier", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of());
    }

    /** Treasure token: colorless artifact with "{T}, Sacrifice this artifact: Add one mana of any color." */
    public static CreateTokenEffect ofTreasureToken(int amount) {
        return ofArtifactToken(amount, "Treasure", List.of(CardSubtype.TREASURE),
                List.of(new ActivatedAbility(
                        true, null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "{T}, Sacrifice this artifact: Add one mana of any color."
                )));
    }
}

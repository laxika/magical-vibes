package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CreateTokenEffect(
        CardType primaryType,
        int amount,
        String tokenName,
        int power,
        int toughness,
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
        boolean legendary
) implements CardEffect {

    /** Single-color creature token (existing pattern) */
    public CreateTokenEffect(String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, 1, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false);
    }

    /** Single-color creature token with amount */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, Map.of(), List.of(), false, false, false);
    }

    /** Multi-color creature token */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors,
                             List<CardSubtype> subtypes) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false);
    }

    /** Multi-color creature token (single) */
    public CreateTokenEffect(String tokenName, int power, int toughness,
                             CardColor color, Set<CardColor> colors,
                             List<CardSubtype> subtypes) {
        this(CardType.CREATURE, 1, tokenName, power, toughness, color, colors, subtypes, Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false);
    }

    /** Single-color creature token, tapped and attacking */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             boolean tappedAndAttacking) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, Set.of(), Set.of(), tappedAndAttacking, false, Map.of(), List.of(), false, false, false);
    }

    /** Single-color creature token with keywords, tapped and attacking, exile at end of combat */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, boolean tappedAndAttacking,
                             boolean exileAtEndOfCombat) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, Set.of(), tappedAndAttacking, false, Map.of(), List.of(), exileAtEndOfCombat, false, false);
    }

    /** Single-color creature token with amount and token effects */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             Map<EffectSlot, CardEffect> tokenEffects) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, false, tokenEffects, List.of(), false, false, false);
    }

    /** Single-color creature token, enters tapped (not attacking) */
    public CreateTokenEffect(int amount, String tokenName, int power, int toughness,
                             CardColor color, List<CardSubtype> subtypes,
                             Set<Keyword> keywords, Set<CardType> additionalTypes,
                             boolean tapped) {
        this(CardType.CREATURE, amount, tokenName, power, toughness, color, null, subtypes, keywords, additionalTypes, false, tapped, Map.of(), List.of(), false, false, false);
    }

    /** Non-creature token with activated abilities (e.g. Treasure, Clue, Food) */
    public static CreateTokenEffect ofArtifactToken(int amount, String tokenName,
                                                     List<CardSubtype> subtypes,
                                                     List<ActivatedAbility> abilities) {
        return new CreateTokenEffect(CardType.ARTIFACT, amount, tokenName, 0, 0, null, null, subtypes, Set.of(), Set.of(), false, false, Map.of(), abilities, false, false, false);
    }

    /** 1/1 white Spirit creature token with flying */
    public static CreateTokenEffect whiteSpirit(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Spirit", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of(), false, false, Map.of(), List.of(), false, false, false);
    }

    /** 2/2 black Zombie creature token */
    public static CreateTokenEffect blackZombie(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Zombie", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false);
    }

    /** 1/1 white Soldier creature token */
    public static CreateTokenEffect whiteSoldier(int amount) {
        return new CreateTokenEffect(CardType.CREATURE, amount, "Soldier", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(), false, false, Map.of(), List.of(), false, false, false);
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

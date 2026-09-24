package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AcademyManufactorTokenReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.AddFrogTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddMapTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddMutagenTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddSoldierTokenToCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddTreasureToFoodTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Applies static replacement effects that change the characteristics of newly created tokens.
 */
public final class TokenCreationReplacementSupport {

    private static final CreateTokenEffect DIVINE_VISITATION_TOKEN = new CreateTokenEffect(
            "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
            Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of());

    private TokenCreationReplacementSupport() {
    }

    /**
     * Returns the replacement token card when a controlled static replacement applies, otherwise
     * returns {@code tokenCard} unchanged.
     */
    public static Card replaceCreatureTokenIfApplicable(GameData gameData, UUID controllerId,
                                                         Card tokenCard) {
        if (!tokenCard.isToken() || !tokenCard.hasType(CardType.CREATURE)
                || !hasCreatureTokenReplacement(gameData, controllerId)) {
            return tokenCard;
        }
        return TokenCardFactory.create(DIVINE_VISITATION_TOKEN, 4, 4, tokenCard.getSetCode());
    }

    /** Applies the replacement to a token permanent that was already wrapped in a Permanent. */
    public static void replaceCreatureTokenIfApplicable(GameData gameData, UUID controllerId,
                                                         Permanent permanent) {
        Card replacement = replaceCreatureTokenIfApplicable(gameData, controllerId, permanent.getCard());
        if (replacement != permanent.getCard()) {
            replacement.freeze();
            permanent.setCard(replacement);
        }
    }

    static int additionalMapTokenCount(GameData gameData, UUID controllerId,
                                       CreateTokenEffect token, int amount) {
        if (amount <= 0 || !isArtifactToken(token)) {
            return 0;
        }
        return additionalMapTokenCount(gameData, controllerId);
    }

    static int additionalMapTokenCount(GameData gameData, UUID controllerId,
                                       Card tokenCard, int amount) {
        if (amount <= 0 || tokenCard == null || !tokenCard.hasType(CardType.ARTIFACT)) {
            return 0;
        }
        return additionalMapTokenCount(gameData, controllerId);
    }

    static CreateTokenEffect additionalMapToken(CreateTokenEffect token) {
        CreateTokenEffect map = CreateTokenEffect.ofMapToken(1);
        return new CreateTokenEffect(
                CardType.ARTIFACT,
                1,
                map.tokenName(),
                0,
                0,
                map.color(),
                map.colors(),
                map.subtypes(),
                map.keywords(),
                map.additionalTypes(),
                false,
                token.tapped() || token.tappedAndAttacking(),
                map.tokenEffects(),
                map.tokenAbilities(),
                token.exileAtEndOfCombat(),
                token.exileAtEndStep(),
                false,
                token.initialPlusOnePlusOneCounters(),
                Set.of());
    }

    static CreateTokenEffect additionalMapToken(boolean tapped, boolean tappedAndAttacking) {
        return CreateTokenEffect.ofMapToken(1).withTapped(tapped || tappedAndAttacking);
    }

    static int additionalMutagenTokenCount(GameData gameData, UUID controllerId, int amount) {
        if (amount <= 0) {
            return 0;
        }
        return countActiveStaticEffects(gameData, controllerId, AddMutagenTokenToTokenCreationEffect.class);
    }

    static CreateTokenEffect additionalMutagenToken(CreateTokenEffect original) {
        CreateTokenEffect mutagen = mutagenToken();
        return new CreateTokenEffect(
                CardType.ARTIFACT,
                1,
                mutagen.tokenName(),
                0,
                0,
                mutagen.color(),
                mutagen.colors(),
                mutagen.subtypes(),
                mutagen.keywords(),
                mutagen.additionalTypes(),
                false,
                original.tapped() || original.tappedAndAttacking(),
                mutagen.tokenEffects(),
                mutagen.tokenAbilities(),
                original.exileAtEndOfCombat(),
                original.exileAtEndStep(),
                false,
                original.initialPlusOnePlusOneCounters(),
                original.grantedKeywordsUntilEndOfTurn(),
                mutagen.supertypes());
    }

    static CreateTokenEffect additionalMutagenToken(boolean tapped, boolean tappedAndAttacking) {
        return mutagenToken().withTapped(tapped || tappedAndAttacking);
    }

    private static CreateTokenEffect mutagenToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Mutagen",
                List.of(),
                List.of(new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(
                                new SacrificeSelfCost(),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. "
                                + "Activate only as a sorcery.",
                        TargetFilters.creature(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                )));
    }

    /** Returns the Frog token blueprint when Quina's token-creation replacement is active. */
    public static CreateTokenEffect additionalFrogTokenIfApplicable(GameData gameData,
                                                                      UUID controllerId,
                                                                      CreateTokenEffect original) {
        if (!hasStaticEffect(gameData, controllerId, AddFrogTokenToTokenCreationEffect.class)) {
            return null;
        }
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Frog", 1, 1, CardColor.GREEN, null,
                List.of(CardSubtype.FROG), Set.of(), Set.of(),
                original.tappedAndAttacking(), original.tapped(), Map.of(), List.of(),
                original.exileAtEndOfCombat(), original.exileAtEndStep(), false,
                original.initialPlusOnePlusOneCounters(), original.grantedKeywordsUntilEndOfTurn(), Set.of());
    }

    /** Returns the Soldier token blueprint when Queen Allenal's replacement applies. */
    public static CreateTokenEffect additionalSoldierTokenIfApplicable(GameData gameData,
                                                                         UUID controllerId,
                                                                         CreateTokenEffect original) {
        return additionalSoldierTokenIfApplicable(gameData, controllerId,
                original.primaryType() == CardType.CREATURE,
                original.tappedAndAttacking(), original.tapped(),
                original.exileAtEndOfCombat(), original.exileAtEndStep(),
                original.initialPlusOnePlusOneCounters(), original.grantedKeywordsUntilEndOfTurn());
    }

    /** Returns the number of active Queen Allenal replacements for a token-creation event. */
    public static int additionalSoldierTokenCountIfApplicable(GameData gameData,
                                                               UUID controllerId,
                                                               CreateTokenEffect original) {
        return additionalSoldierTokenCountIfApplicable(gameData, controllerId,
                original.primaryType() == CardType.CREATURE);
    }

    /** Returns the Soldier token blueprint for a token-copy creation event, when applicable. */
    public static CreateTokenEffect additionalSoldierTokenIfApplicable(GameData gameData,
                                                                         UUID controllerId,
                                                                         boolean creatureTokenEvent,
                                                                         boolean tappedAndAttacking,
                                                                         boolean tapped) {
        return additionalSoldierTokenIfApplicable(gameData, controllerId, creatureTokenEvent,
                tappedAndAttacking, tapped, false, false, 0, Set.of());
    }

    /** Returns the number of active Queen Allenal replacements for a token-copy event. */
    public static int additionalSoldierTokenCountIfApplicable(GameData gameData,
                                                               UUID controllerId,
                                                               boolean creatureTokenEvent) {
        return creatureTokenEvent
                ? staticEffectCount(gameData, controllerId, AddSoldierTokenToCreatureTokenCreationEffect.class)
                : 0;
    }

    private static CreateTokenEffect additionalSoldierTokenIfApplicable(GameData gameData,
                                                                          UUID controllerId,
                                                                          boolean creatureTokenEvent,
                                                                          boolean tappedAndAttacking,
                                                                          boolean tapped,
                                                                          boolean exileAtEndOfCombat,
                                                                          boolean exileAtEndStep,
                                                                          int initialCounters,
                                                                          Set<Keyword> grantedKeywords) {
        if (!creatureTokenEvent
                || !hasStaticEffect(gameData, controllerId, AddSoldierTokenToCreatureTokenCreationEffect.class)) {
            return null;
        }
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Soldier", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(), tappedAndAttacking, tapped,
                Map.of(), List.of(), exileAtEndOfCombat, exileAtEndStep, false,
                initialCounters, grantedKeywords, Set.of());
    }

    /** Returns the number of Treasure tokens added by active Bilbo replacements. */
    static int additionalTreasureTokenCount(GameData gameData, UUID controllerId,
                                             CreateTokenEffect original, int amount) {
        if (amount <= 0 || original.subtypes() == null || !original.subtypes().contains(CardSubtype.FOOD)) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int bilboCount = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(AddTreasureToFoodTokenCreationEffect.class)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AddTreasureToFoodTokenCreationEffect) {
                    bilboCount++;
                }
            }
        }
        return amount * bilboCount;
    }

    /** Returns Bilbo's Treasure token while preserving event-level token riders. */
    static CreateTokenEffect additionalTreasureToken(CreateTokenEffect original) {
        CreateTokenEffect treasure = CreateTokenEffect.ofTreasureToken(1);
        return new CreateTokenEffect(
                CardType.ARTIFACT, 1, treasure.tokenName(), 0, 0, treasure.color(), treasure.colors(),
                treasure.subtypes(), treasure.keywords(), treasure.additionalTypes(), false,
                original.tapped() || original.tappedAndAttacking(), Map.of(), treasure.tokenAbilities(),
                original.exileAtEndOfCombat(), original.exileAtEndStep(), false,
                original.initialPlusOnePlusOneCounters(), Set.of());
    }

    /** Returns Jinnie Fay's replacement token while preserving event-level token riders. */
    public static CreateTokenEffect jinnieFayToken(CreateTokenEffect original, int amount, boolean cat) {
        return new CreateTokenEffect(
                CardType.CREATURE,
                amount,
                cat ? "Cat" : "Dog",
                cat ? 2 : 3,
                cat ? 2 : 1,
                CardColor.GREEN,
                null,
                List.of(cat ? CardSubtype.CAT : CardSubtype.DOG),
                cat ? Set.of(Keyword.HASTE) : Set.of(Keyword.VIGILANCE),
                Set.of(),
                original.tappedAndAttacking(),
                original.tapped(),
                Map.of(),
                List.of(),
                original.exileAtEndOfCombat(),
                original.exileAtEndStep(),
                false,
                original.initialPlusOnePlusOneCounters(),
                original.grantedKeywordsUntilEndOfTurn(),
                Set.of());
    }

    static Card findJinnieFay(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(JinnieFayTokenReplacementEffect.class)) {
                continue;
            }
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(JinnieFayTokenReplacementEffect.class::isInstance)) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private static boolean hasCreatureTokenReplacement(GameData gameData, UUID controllerId) {
        return hasStaticEffect(gameData, controllerId, ReplaceCreatureTokenCreationEffect.class);
    }

    private static boolean hasStaticEffect(GameData gameData, UUID controllerId,
                                            Class<? extends CardEffect> effectType) {
        return staticEffectCount(gameData, controllerId, effectType) > 0;
    }

    private static int staticEffectCount(GameData gameData, UUID controllerId,
                                         Class<? extends CardEffect> effectType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(effectType)) {
                continue;
            }
            count += (int) permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(effectType::isInstance)
                    .count();
        }
        return count;
    }

    private static int additionalMapTokenCount(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AddMapTokenToArtifactTokenCreationEffect) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countActiveStaticEffects(GameData gameData, UUID controllerId,
                                                Class<? extends CardEffect> effectType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown()
                    || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(effectType)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effectType.isInstance(effect)
                        && !permanent.isStaticEffectSuppressed(effect.getClass())) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isArtifactToken(CreateTokenEffect token) {
        return token.primaryType() == CardType.ARTIFACT
                || (token.additionalTypes() != null && token.additionalTypes().contains(CardType.ARTIFACT));
    }
    static List<CreateTokenEffect> additionalAcademyManufactorTokens(
            GameData gameData, UUID controllerId, CreateTokenEffect original, int amount) {
        if (amount <= 0 || original == null) {
            return List.of();
        }
        CardSubtype originalSubtype = manufactorTokenSubtype(original);
        if (originalSubtype == null) {
            return List.of();
        }

        int manufactorCount = academyManufactorCount(gameData, controllerId);
        if (manufactorCount == 0) {
            return List.of();
        }

        int tokensPerSubtype = amount;
        for (int i = 1; i < manufactorCount; i++) {
            tokensPerSubtype = Math.multiplyExact(tokensPerSubtype, 3);
        }

        List<CreateTokenEffect> additional = new ArrayList<>();
        for (int i = amount; i < tokensPerSubtype; i++) {
            additional.add(original);
        }
        for (CardSubtype subtype : List.of(CardSubtype.CLUE, CardSubtype.FOOD, CardSubtype.TREASURE)) {
            if (subtype == originalSubtype) {
                continue;
            }
            CreateTokenEffect standardToken = switch (subtype) {
                case CLUE -> CreateTokenEffect.ofClueToken(1);
                case FOOD -> CreateTokenEffect.ofFoodToken(1);
                case TREASURE -> CreateTokenEffect.ofTreasureToken(1);
                default -> throw new IllegalStateException("Unexpected Academy Manufactor subtype: " + subtype);
            };
            CreateTokenEffect eventToken = withEventModifiers(standardToken, original);
            for (int i = 0; i < tokensPerSubtype; i++) {
                additional.add(eventToken);
            }
        }
        return additional;
    }

    private static CreateTokenEffect withEventModifiers(CreateTokenEffect token,
                                                         CreateTokenEffect original) {
        return new CreateTokenEffect(
                token.primaryType(),
                1,
                token.tokenName(),
                token.tokenPower(),
                token.tokenToughness(),
                token.color(),
                token.colors(),
                token.subtypes(),
                token.keywords(),
                token.additionalTypes(),
                original.tappedAndAttacking(),
                original.tapped(),
                token.tokenEffects(),
                token.tokenAbilities(),
                original.exileAtEndOfCombat(),
                original.exileAtEndStep(),
                token.legendary(),
                original.initialPlusOnePlusOneCounters(),
                original.grantedKeywordsUntilEndOfTurn(),
                token.supertypes());
    }

    private static CardSubtype manufactorTokenSubtype(CreateTokenEffect token) {
        if (token.subtypes() == null) {
            return null;
        }
        for (CardSubtype subtype : List.of(CardSubtype.CLUE, CardSubtype.FOOD, CardSubtype.TREASURE)) {
            if (token.subtypes().contains(subtype)) {
                return subtype;
            }
        }
        return null;
    }

    private static int academyManufactorCount(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(AcademyManufactorTokenReplacementEffect.class)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AcademyManufactorTokenReplacementEffect) {
                    count++;
                }
            }
        }
        return count;
    }
}

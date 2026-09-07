package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddMapTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AddFrogTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCreatureTokenCreationEffect;

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
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance)) {
                return true;
            }
        }
        return false;
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

    private static boolean isArtifactToken(CreateTokenEffect token) {
        return token.primaryType() == CardType.ARTIFACT
                || (token.additionalTypes() != null && token.additionalTypes().contains(CardType.ARTIFACT));
    }
}

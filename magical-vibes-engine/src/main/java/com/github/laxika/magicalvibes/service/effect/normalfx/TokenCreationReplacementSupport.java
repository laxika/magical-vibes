package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AddFrogTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
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
}

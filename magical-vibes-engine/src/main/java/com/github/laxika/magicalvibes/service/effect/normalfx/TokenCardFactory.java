package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the {@link Card} for one token from its {@link CreateTokenEffect} blueprint — the single
 * place that knows how a blueprint becomes a card.
 *
 * <p>Both token-creation paths ({@link PermanentControlSupport#applyCreateToken} and
 * {@link DestructionSupport#createTokenForPlayer}) assembled this card field by field themselves,
 * and the two copies drifted: the destruction path silently dropped the blueprint's extra colors,
 * legendary supertype, effects and abilities, so Harsh Annotation's white-and-black Inkling entered
 * mono-white.
 *
 * <p>Card-level only. Everything belonging to the resulting permanent rather than the card —
 * entering tapped or attacking, +1/+1 counters, end-step exile riders — stays with the caller.
 */
final class TokenCardFactory {

    private TokenCardFactory() {
    }

    /**
     * @param power         already-evaluated printed power (dynamic X/X blueprints are resolved by
     *                      the caller); ignored for a non-creature token
     * @param toughness     already-evaluated printed toughness; ignored for a non-creature token
     * @param sourceSetCode preferred set for token art (the creating card's set), or {@code null}
     *                      to leave the token artless — for callers that no longer know the card that
     *                      created it. When the preferred set has no matching token,
     *                      {@link CardPrintingRegistry#getTokenImage} falls back to another
     *                      registered set that does.
     */
    static Card create(CreateTokenEffect token, int power, int toughness, String sourceSetCode) {
        boolean isCreature = token.primaryType() == CardType.CREATURE;

        Card tokenCard = new Card();
        tokenCard.setName(token.tokenName());
        tokenCard.setType(token.primaryType());
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(token.color());
        if (token.colors() != null && !token.colors().isEmpty()) {
            tokenCard.setColors(token.colors().stream().toList());
        }
        if (isCreature || power != 0 || toughness != 0) {
            tokenCard.setPower(power);
            tokenCard.setToughness(toughness);
        }
        tokenCard.setSubtypes(token.subtypes());
        if (token.keywords() != null && !token.keywords().isEmpty()) {
            tokenCard.setKeywords(token.keywords());
            tokenCard.setCardText(keywordText(token.keywords()));
        }
        if (token.additionalTypes() != null && !token.additionalTypes().isEmpty()) {
            tokenCard.setAdditionalTypes(token.additionalTypes());
        }
        if (token.legendary()) {
            tokenCard.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        }
        if (token.tokenEffects() != null) {
            for (Map.Entry<EffectSlot, CardEffect> tokenEffect : token.tokenEffects().entrySet()) {
                if (tokenEffect.getKey() == EffectSlot.STATIC
                        && tokenEffect.getValue() instanceof SequenceEffect sequence) {
                    // Token blueprints use a sequence to carry multiple static abilities through
                    // the one-effect-per-slot blueprint map; live token cards keep each ability
                    // as an ordinary static effect for the layered and combat queries.
                    for (CardEffect step : sequence.steps()) {
                        tokenCard.addEffect(EffectSlot.STATIC, step);
                    }
                } else {
                    tokenCard.addEffect(tokenEffect.getKey(), tokenEffect.getValue());
                }
            }
        }
        if (token.tokenAbilities() != null) {
            for (ActivatedAbility ability : token.tokenAbilities()) {
                tokenCard.addActivatedAbility(ability);
            }
        }

        CardPrintingRegistry.TokenImageData imageData = isCreature
                ? CardPrintingRegistry.getTokenImage(sourceSetCode, token.tokenName(), power, toughness, token.color())
                : CardPrintingRegistry.getTokenImage(sourceSetCode, token.tokenName(), token.color());
        if (imageData != null) {
            tokenCard.setSetCode(imageData.setCode());
            tokenCard.setCollectorNumber(imageData.collectorNumber());
        }
        return tokenCard;
    }

    /**
     * The text box of a token, which has no Scryfall oracle text to print its keywords from: the
     * keyword line as a real token card prints it ("Trample, Reach"). Without it a token's keywords
     * render nowhere, because the card face shows {@code cardText} plus only those keywords
     * <em>granted</em> on top of the printed ones.
     *
     * <p>Every keyword is capitalized (matching the frontend's {@code formatKeywords()}), and the
     * {@link EnumSet} copy fixes the order at declaration order whatever {@link Set} the blueprint
     * carries — otherwise the same token could read differently from one game to the next.
     */
    private static String keywordText(Set<Keyword> keywords) {
        return EnumSet.copyOf(keywords).stream()
                .map(keyword -> {
                    String words = keyword.name().toLowerCase(Locale.ROOT).replace('_', ' ');
                    return Character.toUpperCase(words.charAt(0)) + words.substring(1);
                })
                .collect(Collectors.joining(", "));
    }
}

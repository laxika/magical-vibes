package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceTokenSubtypeCreationEffect;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The tokens replacement effects put in place of or beside a token creation event's tokens (Divine Visitation, Fisher's
 * Talent, Jinnie Fay, every {@code AddTokenCreationEffect}), each keeping the creating effect's riders.
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
        if (!tokenCard.isToken()) {
            return tokenCard;
        }
        Card replacedToken = replaceTokenSubtypeIfApplicable(gameData, controllerId, tokenCard);
        if (!entersAsCreature(replacedToken)
                || !hasCreatureTokenReplacement(gameData, controllerId)) {
            return replacedToken;
        }
        return TokenCardFactory.create(DIVINE_VISITATION_TOKEN, 4, 4, replacedToken.getSetCode());
    }

    /**
     * Whether the token is a creature as it enters. A token whose own static ability sets its card
     * types without Creature (a copy that "is an artifact and loses all other card types") is not
     * a creature token, so creature-token replacements such as Divine Visitation do not apply.
     */
    public static boolean entersAsCreature(Card tokenCard) {
        return tokenCard.hasType(CardType.CREATURE)
                && tokenCard.getStaticEffects().stream()
                .noneMatch(effect -> effect instanceof SetCardTypesEffect set
                        && set.subject() instanceof PermanentSubject.Source
                        && !set.cardTypes().contains(CardType.CREATURE));
    }

    /**
     * A blueprint describing copies of {@code tokenCard} (its types, subtypes, name and colour) for the token
     * creation replacements that look at the kind of token being created; the copies themselves are built from
     * the card, not from this blueprint.
     */
    public static CreateTokenEffect copyKind(Card tokenCard) {
        return new CreateTokenEffect(tokenCard.getType(), 1, tokenCard.getName(), 0, 0, tokenCard.getColor(),
                null, List.copyOf(tokenCard.getSubtypes()), Set.of(), Set.copyOf(tokenCard.getAdditionalTypes()),
                false, false, List.of(), List.of(), false, false, false, 0, Set.of(), Set.of());
    }

    /**
     * Divine Visitation's 4/4 white Angel created in place of a creature token of {@code original}, keeping the
     * creating effect's riders (tapped, attacking, exiled at end of combat, ...) but none of the replaced token's
     * characteristics or abilities.
     */
    public static CreateTokenEffect divineVisitationToken(CreateTokenEffect original) {
        return withEventModifiers(DIVINE_VISITATION_TOKEN, original);
    }

    /**
     * The token {@code replacement} (Fisher's Talent's Shark or Octopus) creates in place of a token of
     * {@code original}, keeping the creating effect's riders but none of the replaced token's characteristics or
     * abilities.
     */
    public static CreateTokenEffect subtypeReplacementToken(ReplaceTokenSubtypeCreationEffect replacement,
                                                            CreateTokenEffect original) {
        return withEventModifiers(replacement.replacementToken(), original);
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
                List.of(),
                List.of(),
                original.exileAtEndOfCombat(),
                original.exileAtEndStep(),
                false,
                original.initialPlusOnePlusOneCounters(),
                original.grantedKeywordsUntilEndOfTurn(),
                Set.of());
    }

    /**
     * {@code event} after Jinnie Fay's replacement: "that many" Cats or Dogs, one for every token the event creates as
     * it stands (its blueprint's tokens, simultaneous tokens and tokens additions put in it alike), keeping the
     * creating effect's riders (tapped, attacking, exiled at end of combat, ...) but none of the replaced tokens'
     * characteristics or abilities.
     *
     * <p>In a token-copy event ({@link TokenCreationEvent#copyBatch()}) the Cats or Dogs are substitute groups
     * ({@link TokenCreationEvent.TokenGroup#substitutes}), one per copied card, so each keeps the creating effect's
     * instructions for the copies it replaces (Mirror Match's "blocking that creature", the copies' attack target),
     * and a doubler applied after Jinnie Fay doubles them per copied card.
     */
    public static TokenCreationEvent jinnieFayEvent(TokenCreationEvent event, boolean cat) {
        if (event.copyBatch() != null) {
            CreateTokenEffect substitute = jinnieFayToken(event.token(), 1, cat);
            List<TokenCreationEvent.TokenGroup> groups = new ArrayList<>();
            if (event.tokenCount() > 0) {
                groups.add(TokenCreationEvent.TokenGroup.substitutes(substitute, event.tokenCount(), event.copyOf()));
            }
            for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
                groups.add(TokenCreationEvent.TokenGroup.substitutes(substitute, group.count(), group.copyOf()));
            }
            return new TokenCreationEvent(event.controllerId(), 0, event.appliedKeys(), substitute, cat ? 2 : 3,
                    cat ? 2 : 1, event.sourceSetCode(), List.of(), true, groups, event.copyOf(), event.copyBatch());
        }
        return new TokenCreationEvent(event.controllerId(), event.amount(), event.appliedKeys(),
                jinnieFayToken(event.token(), 1, cat), cat ? 2 : 3, cat ? 2 : 1, event.sourceSetCode(), List.of(),
                true, List.of());
    }

    private static boolean hasCreatureTokenReplacement(GameData gameData, UUID controllerId) {
        return hasStaticEffect(gameData, controllerId, ReplaceCreatureTokenCreationEffect.class);
    }

    private static Card replaceTokenSubtypeIfApplicable(GameData gameData, UUID controllerId,
                                                         Card tokenCard) {
        Card replaced = tokenCard;
        for (ReplaceTokenSubtypeCreationEffect effect : activeTokenSubtypeReplacements(
                gameData, controllerId)) {
            if (replaced.getSubtypes() != null
                    && replaced.getSubtypes().contains(effect.sourceSubtype())) {
                CreateTokenEffect replacement = effect.replacementToken();
                replaced = TokenCardFactory.create(
                        replacement,
                        replacement.tokenPower(),
                        replacement.tokenToughness(),
                        replaced.getSetCode());
            }
        }
        return replaced;
    }

    private static List<ReplaceTokenSubtypeCreationEffect> activeTokenSubtypeReplacements(
            GameData gameData, UUID controllerId) {
        List<ReplaceTokenSubtypeCreationEffect> replacements = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (permanent.isFaceDown() || gameData.losesAllAbilitiesByEffect(permanent)) {
                continue;
            }
            int sourceLevel = permanent.getClassLevel();
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof ReplaceTokenSubtypeCreationEffect replacement
                        && sourceLevel >= replacement.minimumSourceLevel()
                        && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) {
                    replacements.add(replacement);
                }
            }
        }
        replacements.sort(java.util.Comparator.comparingInt(
                ReplaceTokenSubtypeCreationEffect::minimumSourceLevel));
        return replacements;
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
            if (permanent.isFaceDown()
                    || gameData.losesAllAbilitiesByEffect(permanent)
                    || gameData.removesOwnStaticAbility(permanent.getId(), effectType)) {
                continue;
            }
            count += (int) permanent.getCard().getStaticEffects().stream()
                    .filter(effectType::isInstance)
                    .count();
        }
        return count;
    }

    /**
     * One token of {@code token} created by an event whose creating effect made {@code original}: the token's own
     * characteristics and abilities with the effect's riders (tapped, attacking, "gains haste until end of turn",
     * exiled at end of combat or at the end step, entering with +1/+1 counters), never {@code original}'s
     * characteristics or abilities (Chatterfang's ruling). A noncreature token can't attack (CR 506.3), so it enters
     * tapped where the effect's tokens enter tapped and attacking. Used for the tokens replacements add to or put in
     * place of the event's tokens.
     */
    public static CreateTokenEffect withEventModifiers(CreateTokenEffect token, CreateTokenEffect original) {
        boolean attacking = token.primaryType() == CardType.CREATURE && original.tappedAndAttacking();
        return new CreateTokenEffect(
                token.primaryType(),
                new Fixed(1),
                token.tokenName(),
                token.power(),
                token.toughness(),
                token.color(),
                token.colors(),
                token.subtypes(),
                token.keywords(),
                token.additionalTypes(),
                attacking,
                original.tapped() || (original.tappedAndAttacking() && !attacking),
                token.tokenEffects(),
                token.tokenAbilities(),
                original.exileAtEndOfCombat(),
                original.exileAtEndStep(),
                token.legendary(),
                original.initialPlusOnePlusOneCounters(),
                original.grantedKeywordsUntilEndOfTurn(),
                token.supertypes(),
                token.tokenTargetFilter(),
                false,
                List.of(),
                attacking && original.attackingUntapped(),
                token.manaCost(),
                token.controller());
    }
}

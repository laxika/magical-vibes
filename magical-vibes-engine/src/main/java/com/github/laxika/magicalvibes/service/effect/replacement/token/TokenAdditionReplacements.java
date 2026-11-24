package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCreationReplacementSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The {@link AddTokenCreationEffect} replacements that apply to a token creation event, one option per active static
 * effect instance of the permanents the tokens' creator controls: Xorn's Treasure, Queen Allenal of Ruadach's Soldier,
 * Chatterfang's Squirrels, Bilbo's Treasures, Academy Manufactor's "one of each" and the rest. The creator is the
 * player under whose control the tokens would be created once Crafty Cutpurse's replacement has applied (Chatterfang's
 * ruling). They only rewrite the event, so they are pure; ordered with the count replacements, a doubler applied after
 * an addition doubles the added tokens too, and an addition applies to tokens another replacement put in the event
 * before it (CR 616.1f).
 */
@Component
public class TokenAdditionReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    /** Reads a permanent's active static abilities, a static "loses all abilities" and a solved Case included. */
    @Autowired
    @Lazy
    private GameQueryService gameQueryService;

    private static final String KEY_PREFIX = "TokenAddition:";

    private record Instance(String key, String label, AddTokenCreationEffect addition) {}

    @Override
    public Class<TokenCreationEvent> eventType() {
        return TokenCreationEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, TokenCreationEvent event) {
        return instances(gameData, event).stream()
                .map(instance -> new ReplacementOption(instance.key(), instance.label(),
                        ReplacementOption.Kind.REPLACEMENT, true))
                .toList();
    }

    @Override
    public TokenCreationEvent transform(GameData gameData, TokenCreationEvent event, ReplacementOption option) {
        AddTokenCreationEffect addition = instances(gameData, event).stream()
                .filter(candidate -> candidate.key().equals(option.key()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token addition replacement source left: " + option.key()))
                .addition();
        if (addition.count() == AddTokenCreationEffect.Count.ONE) {
            TokenCreationEvent result = event;
            for (CreateTokenEffect token : addition.tokens()) {
                result = result.plus(TokenCreationReplacementSupport.withEventModifiers(token, event.token()), 1);
            }
            return result;
        }
        TokenCreationEvent result = event;
        if (event.tokenCount() > 0 && addition.when().matches(event.token(), event.creatureToken())) {
            for (CreateTokenEffect added : additionsFor(addition, event.token())) {
                result = result.plus(added, event.tokenCount());
            }
        }
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            if (addition.when().matches(group.blueprint(), group.creature())) {
                for (CreateTokenEffect added : additionsFor(addition, group.blueprint())) {
                    result = result.plus(added, group.count());
                }
            }
        }
        return result;
    }

    /**
     * The tokens {@code addition} adds for one token of {@code original}'s kind, each with the riders of
     * {@code original}: every blueprint, or for {@link AddTokenCreationEffect.Count#ONE_OF_EACH} those sharing no
     * subtype with it.
     */
    private static List<CreateTokenEffect> additionsFor(AddTokenCreationEffect addition, CreateTokenEffect original) {
        List<CreateTokenEffect> added = new ArrayList<>(addition.tokens().size());
        for (CreateTokenEffect token : addition.tokens()) {
            if (addition.count() == AddTokenCreationEffect.Count.ONE_OF_EACH && sharesSubtype(token, original)) {
                continue;
            }
            added.add(TokenCreationReplacementSupport.withEventModifiers(token, original));
        }
        return added;
    }

    private static boolean sharesSubtype(CreateTokenEffect token, CreateTokenEffect original) {
        return token.subtypes() != null && original.subtypes() != null
                && token.subtypes().stream().anyMatch(original.subtypes()::contains);
    }

    /** Whether {@code addition} adds a token to {@code event} as it stands (CR 616.1f). */
    private static boolean applies(AddTokenCreationEffect addition, TokenCreationEvent event) {
        if (event.tokenCount() > 0 && addition.when().matches(event.token(), event.creatureToken())
                && (addition.count() == AddTokenCreationEffect.Count.ONE
                || !additionsFor(addition, event.token()).isEmpty())) {
            return true;
        }
        return event.addedTokens().stream().anyMatch(group -> addition.when().matches(group.blueprint(), group.creature())
                && (addition.count() == AddTokenCreationEffect.Count.ONE
                || !additionsFor(addition, group.blueprint()).isEmpty()));
    }

    private List<Instance> instances(GameData gameData, TokenCreationEvent event) {
        if (event.amount() <= 0) {
            return List.of();
        }
        UUID creatorId = gameQueryService.resolveTokenCreationController(gameData, event.controllerId(), false);
        List<Instance> instances = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(creatorId, List.of())) {
            List<CardEffect> statics = gameQueryService.getActiveStaticEffects(gameData, permanent, creatorId);
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof AddTokenCreationEffect addition
                        && !gameData.removesOwnStaticAbility(permanent.getId(), AddTokenCreationEffect.class)
                        && applies(addition, event)) {
                    instances.add(new Instance(KEY_PREFIX + permanent.getId() + ":" + i,
                            FaceDownVisibility.publicName(gameData, permanent) + ": " + describe(addition),
                            addition));
                }
            }
        }
        return instances;
    }

    private static String describe(AddTokenCreationEffect addition) {
        String tokens = addition.tokens().stream().map(TokenAdditionReplacements::describe)
                .collect(Collectors.joining(" and "));
        String kind = describe(addition.when());
        return switch (addition.count()) {
            case ONE -> "create an additional " + tokens + " token";
            case PER_TOKEN -> "create that many additional " + tokens + " tokens";
            case ONE_OF_EACH -> "create one each of " + tokens + " for each " + kind + " token";
        };
    }

    private static String describe(CreateTokenEffect token) {
        return token.primaryType() == CardType.CREATURE
                ? token.tokenPower() + "/" + token.tokenToughness() + " " + token.tokenName()
                : token.tokenName();
    }

    private static String describe(AddTokenCreationEffect.TokenKind kind) {
        if (!kind.subtypes().isEmpty()) {
            return kind.subtypes().stream().map(subtype -> subtype.getDisplayName()).sorted()
                    .collect(Collectors.joining(", ", "", ""));
        }
        return kind.type() == null ? "" : kind.type().name().toLowerCase();
    }
}

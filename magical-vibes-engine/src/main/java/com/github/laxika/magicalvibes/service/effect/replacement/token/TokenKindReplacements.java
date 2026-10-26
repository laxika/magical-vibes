package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceTokenSubtypeCreationEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCreationReplacementSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Replacements that change what kind of token a token creation event creates, one option per effect instance:
 * Divine Visitation's "that many 4/4 white Angel creature tokens ... instead" for every creature token and Fisher's
 * Talent's Shark instead of each Fish token and Octopus instead of each Shark token. Ordered with the count
 * replacements, additions and Jinnie Fay's replacement (CR 616.1): a Soldier Queen Allenal of Ruadach adds after
 * Divine Visitation applied stays a Soldier, one added before it becomes an Angel too. They only rewrite the event, so
 * they are pure. The replaced tokens keep the creating effect's riders (tapped, attacking, exiled at end of combat)
 * but none of their characteristics or abilities.
 *
 * <p>Only blueprint events are ordered here; token-copy events ({@link TokenCreationEvent#copyBatch()}) still apply
 * these replacements to each copy after the order.
 */
@Component
public class TokenKindReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    private static final String KEY_PREFIX = "TokenKind:";

    private record Instance(String key, String label, Predicate<CreateTokenEffect> replaces,
                            UnaryOperator<CreateTokenEffect> replacement) {}

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
        Instance instance = instances(gameData, event).stream()
                .filter(candidate -> candidate.key().equals(option.key()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token kind replacement source left: " + option.key()));
        TokenCreationEvent result = event;
        if (event.tokenCount() > 0 && instance.replaces().test(event.token())) {
            CreateTokenEffect replaced = instance.replacement().apply(event.token());
            result = result.withToken(replaced, replaced.tokenPower(), replaced.tokenToughness(),
                    replaced.primaryType() == CardType.CREATURE);
        }
        List<TokenCreationEvent.TokenGroup> groups = new ArrayList<>(event.addedTokens().size());
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            groups.add(instance.replaces().test(group.blueprint())
                    ? new TokenCreationEvent.TokenGroup(instance.replacement().apply(group.blueprint()), group.count())
                    : group);
        }
        return result.withAddedTokens(groups);
    }

    private List<Instance> instances(GameData gameData, TokenCreationEvent event) {
        if (event.amount() <= 0 || event.copyBatch() != null) {
            return List.of();
        }
        List<Instance> instances = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(event.controllerId(), List.of())) {
            if (permanent.isFaceDown() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            List<CardEffect> statics = permanent.getCard().getEffects(EffectSlot.STATIC);
            for (int i = 0; i < statics.size(); i++) {
                CardEffect effect = statics.get(i);
                if (permanent.isStaticEffectSuppressed(effect.getClass())) {
                    continue;
                }
                String key = KEY_PREFIX + permanent.getId() + ":" + i;
                String name = permanent.getCard().getName();
                Instance instance = switch (effect) {
                    case ReplaceCreatureTokenCreationEffect ignored -> new Instance(key,
                            name + ": create that many 4/4 Angel tokens instead of creature tokens",
                            blueprint -> blueprint.primaryType() == CardType.CREATURE,
                            TokenCreationReplacementSupport::divineVisitationToken);
                    case ReplaceTokenSubtypeCreationEffect subtype
                            when permanent.getClassLevel() >= subtype.minimumSourceLevel() -> new Instance(key,
                            name + ": create " + subtype.replacementToken().tokenName() + " tokens instead of "
                                    + subtype.sourceSubtype().getDisplayName() + " tokens",
                            blueprint -> hasSubtype(blueprint, subtype.sourceSubtype()),
                            blueprint -> TokenCreationReplacementSupport.subtypeReplacementToken(subtype, blueprint));
                    default -> null;
                };
                if (instance != null && event.createsToken(instance.replaces())) {
                    instances.add(instance);
                }
            }
        }
        return instances;
    }

    private static boolean hasSubtype(CreateTokenEffect blueprint, CardSubtype subtype) {
        return blueprint.subtypes() != null && blueprint.subtypes().contains(subtype);
    }
}

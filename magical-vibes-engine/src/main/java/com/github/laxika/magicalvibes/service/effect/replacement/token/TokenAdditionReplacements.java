package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddFrogTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddSoldierTokenToCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddSquirrelTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCreationReplacementSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * "Those tokens plus ..." replacements that add tokens of another kind to a token creation event
 * under their controller's control, one option per effect instance: Queen Allenal of Ruadach's
 * Soldier (creature tokens only), Quina Qu Gourmet's Frog and Chatterfang's "that many" Squirrels.
 * They only rewrite the event, so they are pure; ordered with the count replacements, a doubler
 * applied after an addition doubles the added tokens too (CR 616.1).
 */
@Component
public class TokenAdditionReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    private static final String KEY_PREFIX = "TokenAddition:";

    private enum Addition {
        SOLDIER(AddSoldierTokenToCreatureTokenCreationEffect.class, "create an additional 1/1 Soldier token"),
        FROG(AddFrogTokenToTokenCreationEffect.class, "create an additional 1/1 Frog token"),
        SQUIRREL(AddSquirrelTokenToTokenCreationEffect.class, "create that many additional 1/1 Squirrel tokens");

        private final Class<? extends CardEffect> effectType;
        private final String description;

        Addition(Class<? extends CardEffect> effectType, String description) {
            this.effectType = effectType;
            this.description = description;
        }

        static Addition of(CardEffect effect) {
            for (Addition addition : values()) {
                if (addition.effectType.isInstance(effect)) {
                    return addition;
                }
            }
            return null;
        }
    }

    private record Instance(String key, String label, Addition addition) {}

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
                .orElseThrow(() -> new IllegalStateException("Token addition replacement source left: " + option.key()));
        CreateTokenEffect original = event.token();
        return switch (instance.addition()) {
            case SOLDIER -> event.plus(TokenCreationReplacementSupport.additionalSoldierToken(original), 1);
            case FROG -> event.plus(TokenCreationReplacementSupport.additionalFrogToken(original), 1);
            case SQUIRREL -> event.plus(TokenCreationReplacementSupport.additionalSquirrelToken(original),
                    event.amount());
        };
    }

    private List<Instance> instances(GameData gameData, TokenCreationEvent event) {
        if (event.amount() <= 0) {
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
                Addition addition = Addition.of(effect);
                if (addition == null || permanent.isStaticEffectSuppressed(effect.getClass())
                        || addition == Addition.SOLDIER && !event.createsCreatureToken()) {
                    continue;
                }
                instances.add(new Instance(KEY_PREFIX + permanent.getId() + ":" + i,
                        permanent.getCard().getName() + ": " + addition.description, addition));
            }
        }
        return instances;
    }
}

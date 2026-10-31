package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AcademyManufactorTokenReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.AddClueTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddFoodTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddFrogTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddMapTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddMutagenTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddSoldierTokenToCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddSquirrelTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddThopterTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.AddTreasureToFoodTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCreationReplacementSupport;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * "Those tokens plus ..." replacements that add tokens of another kind to a token creation event
 * under their controller's control, one option per effect instance: Queen Allenal of Ruadach's
 * Soldier (creature tokens only), Quina Qu Gourmet's Frog, Chatterfang's "that many" Squirrels,
 * Worldwalker Helm's Map and Stridehangar Automaton's Thopter (artifact tokens only), Donatello's
 * Mutagen, Tippy-Toe's Food, a solved Case of the Pilfered Proof's Clue, Bilbo's Treasure for each
 * Food token and Academy Manufactor's "one of each" for every Clue, Food and Treasure token. They only
 * rewrite the event, so they are pure; ordered with the count replacements, a doubler applied after an
 * addition doubles the added tokens too, and an addition applies to tokens another addition put in the
 * event before it (CR 616.1f).
 */
@Component
public class TokenAdditionReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    private static final String KEY_PREFIX = "TokenAddition:";

    private enum Addition {
        SOLDIER(AddSoldierTokenToCreatureTokenCreationEffect.class, "create an additional 1/1 Soldier token"),
        FROG(AddFrogTokenToTokenCreationEffect.class, "create an additional 1/1 Frog token"),
        SQUIRREL(AddSquirrelTokenToTokenCreationEffect.class, "create that many additional 1/1 Squirrel tokens"),
        MAP(AddMapTokenToArtifactTokenCreationEffect.class, "create an additional Map token"),
        THOPTER(AddThopterTokenToArtifactTokenCreationEffect.class, "create an additional 1/1 Thopter token"),
        MUTAGEN(AddMutagenTokenToTokenCreationEffect.class, "create an additional Mutagen token"),
        FOOD(AddFoodTokenToTokenCreationEffect.class, "create an additional Food token"),
        CLUE(AddClueTokenToTokenCreationEffect.class, "create an additional Clue token"),
        TREASURE(AddTreasureToFoodTokenCreationEffect.class, "create a Treasure token for each Food token"),
        ONE_OF_EACH(AcademyManufactorTokenReplacementEffect.class,
                "create a Clue, a Food and a Treasure token for each Clue, Food and Treasure token");

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
            case MAP -> event.plus(TokenCreationReplacementSupport.additionalMapToken(original), 1);
            case THOPTER -> event.plus(TokenCreationReplacementSupport.additionalThopterToken(original), 1);
            case MUTAGEN -> event.plus(TokenCreationReplacementSupport.additionalMutagenToken(original), 1);
            case FOOD -> event.plus(TokenCreationReplacementSupport.additionalFoodToken(original), 1);
            case CLUE -> event.plus(TokenCreationReplacementSupport.additionalClueToken(original), 1);
            case TREASURE -> forEachToken(event, blueprint -> TokenCreationReplacementSupport.isFoodToken(blueprint)
                    ? List.of(TokenCreationReplacementSupport.additionalTreasureToken(blueprint)) : List.of());
            case ONE_OF_EACH -> forEachToken(event, TokenCreationReplacementSupport::academyManufactorAdditions);
        };
    }

    /**
     * {@code event} with the tokens {@code additions} lists for each token it creates, each carrying the
     * riders of the token it was added for.
     */
    private static TokenCreationEvent forEachToken(TokenCreationEvent event,
                                                   Function<CreateTokenEffect, List<CreateTokenEffect>> additions) {
        TokenCreationEvent result = event;
        if (event.tokenCount() > 0) {
            for (CreateTokenEffect added : additions.apply(event.token())) {
                result = result.plus(added, event.tokenCount());
            }
        }
        for (TokenCreationEvent.TokenGroup group : event.addedTokens()) {
            for (CreateTokenEffect added : additions.apply(group.blueprint())) {
                result = result.plus(added, group.count());
            }
        }
        return result;
    }

    private static boolean applies(Addition addition, Permanent permanent, TokenCreationEvent event) {
        return switch (addition) {
            case SOLDIER -> event.createsCreatureToken();
            case MAP, THOPTER -> event.createsToken(TokenCreationReplacementSupport::isArtifactToken);
            case TREASURE -> event.createsToken(TokenCreationReplacementSupport::isFoodToken);
            case ONE_OF_EACH -> event.createsToken(blueprint ->
                    !TokenCreationReplacementSupport.academyManufactorAdditions(blueprint).isEmpty());
            case CLUE -> permanent.isSolved();
            case FROG, SQUIRREL, MUTAGEN, FOOD -> true;
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
            List<CardEffect> statics = permanent.getCard().getStaticEffects();
            for (int i = 0; i < statics.size(); i++) {
                CardEffect effect = statics.get(i);
                Addition addition = Addition.of(effect);
                if (addition == null || permanent.isStaticEffectSuppressed(effect.getClass())
                        || !applies(addition, permanent, event)) {
                    continue;
                }
                instances.add(new Instance(KEY_PREFIX + permanent.getId() + ":" + i,
                        permanent.getCard().getName() + ": " + addition.description, addition));
            }
        }
        return instances;
    }
}

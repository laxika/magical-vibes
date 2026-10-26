package com.github.laxika.magicalvibes.service.effect.replacement.token;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.JinnieFayTokenReplacementEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementCandidateSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Jinnie Fay, Jetmir's Second's "you may instead create that many 2/2 green Cat creature tokens with haste or that
 * many 3/1 green Dog creature tokens with vigilance", one option per effect instance, ordered with the token count
 * replacements and additions of a blueprint token creation event (CR 616.1): applied after Tippy-Toe or a doubler
 * it replaces the tokens they added or doubled too, applied before them the additions and doublers see the Cats or
 * Dogs. Applying it asks the creating player for Cat, Dog or the original tokens, so it is not pure; the event
 * continues through {@code ChoiceHandlerService} once they answer.
 *
 * <p>Token-copy events ({@link TokenCreationEvent#copyBatch()}) still ask about Jinnie Fay after their other
 * replacements, in {@code TokenCopySupport}, so they are left out here.
 */
@Component
public class JinnieFayTokenReplacements implements ReplacementCandidateSource<TokenCreationEvent> {

    private static final String KEY_PREFIX = "JinnieFay:";

    private final PlayerInputService playerInputService;

    public JinnieFayTokenReplacements(@Lazy PlayerInputService playerInputService) {
        this.playerInputService = playerInputService;
    }

    private record Instance(String key, Permanent source) {}

    @Override
    public Class<TokenCreationEvent> eventType() {
        return TokenCreationEvent.class;
    }

    @Override
    public List<ReplacementOption> applicable(GameData gameData, TokenCreationEvent event) {
        return instances(gameData, event).stream()
                .map(instance -> new ReplacementOption(instance.key(),
                        instance.source().getCard().getName()
                                + ": you may instead create that many Cat or Dog tokens",
                        ReplacementOption.Kind.REPLACEMENT, false))
                .toList();
    }

    @Override
    public TokenCreationEvent transform(GameData gameData, TokenCreationEvent event, ReplacementOption option) {
        throw new IllegalStateException("Jinnie Fay's replacement asks the player and is never previewed");
    }

    @Override
    public ApplyResult<TokenCreationEvent> apply(GameData gameData, TokenCreationEvent event,
                                                 ReplacementOption option) {
        Permanent source = instances(gameData, event).stream()
                .filter(instance -> instance.key().equals(option.key()))
                .map(Instance::source)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Jinnie Fay replacement source left: " + option.key()));
        playerInputService.beginJinnieFayTokenChoice(gameData,
                new ChoiceContext.JinnieFayTokenChoice(event.controllerId(), source.getCard(), event));
        return new ApplyResult.Suspended<>(event);
    }

    private List<Instance> instances(GameData gameData, TokenCreationEvent event) {
        if (event.copyBatch() != null || event.amount() <= 0) {
            return List.of();
        }
        List<Instance> instances = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(event.controllerId(), List.of())) {
            if (permanent.isFaceDown() || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(JinnieFayTokenReplacementEffect.class)) {
                continue;
            }
            List<CardEffect> statics = permanent.getCard().getEffects(EffectSlot.STATIC);
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof JinnieFayTokenReplacementEffect) {
                    instances.add(new Instance(KEY_PREFIX + permanent.getId() + ":" + i, permanent));
                }
            }
        }
        return instances;
    }
}

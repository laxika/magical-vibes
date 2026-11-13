package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves "create a token. When you do, ..." (CR 603.12): the tokens are created, then the follow-up is put on the
 * stack as a reflexive ability once for each time the written action occurred (CR 603.12a), or, for a follow-up the
 * card writes as about "that token", one for each token created, with that token as the permanent it acts for.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenThenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenThenEffect createThen = (CreateTokenThenEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenEffectHandler.resolve(gameData, entry, createThen.tokenEffect());
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (entry.getCreatedPermanentIds().size() == createdBefore) {
            return;
        }

        if (!createThen.aboutThatToken()) {
            // The ability triggers once for each time the written action occurred (CR 603.12a). Each written quantity
            // of the written tokens a replacement effect makes counts as the action done once more (Ajani, Nacatl
            // Avenger's ruling for one token: a doubler makes two abilities; "create two" under one doubler makes four
            // tokens, two abilities; a tripler three). Tokens a replacement effect creates instead (Divine
            // Visitation's Angels) or adds of another kind (Chatterfang's Squirrels) aren't the written tokens. No
            // ruling covers a quantity above one, and a count that isn't a multiple of it rounds down. A quantity
            // that isn't fixed triggers it once.
            long writtenTokens = entry.getCreatedPermanentIds().subList(createdBefore,
                    entry.getCreatedPermanentIds().size()).stream()
                    .map(id -> gameQueryService.findPermanentById(gameData, id))
                    .filter(token -> token != null && createThen.tokenEffect().tokenName() != null
                            && createThen.tokenEffect().tokenName().equals(token.getCard().getName()))
                    .count();
            int writtenQuantity = createThen.tokenEffect().amount() instanceof Fixed fixed ? fixed.value() : 0;
            long count = writtenQuantity > 0 ? writtenTokens / writtenQuantity : Math.min(1, writtenTokens);
            List<CardEffect> reflexive = new ArrayList<>();
            for (long i = 0; i < count; i++) {
                reflexive.add(new QueueReflexiveAbilityEffect(createThen.thenEffect()));
            }
            queueEachAsAStep(gameData, entry, reflexive);
            return;
        }
        // A replacement effect that makes more tokens makes "that token" each created token in turn. The tokens are
        // the entry's picks, and each ability is put on the stack as a step of this resolution that takes the next
        // one, so one's target is chosen before the next one's is asked.
        List<UUID> createdIds = List.copyOf(entry.getCreatedPermanentIds()
                .subList(createdBefore, entry.getCreatedPermanentIds().size()));
        entry.recordPicks(createdIds);
        List<CardEffect> abilities = new ArrayList<>();
        for (int i = 0; i < createdIds.size(); i++) {
            abilities.add(QueueReflexiveAbilityEffect.aboutPickedPermanent(createThen.thenEffect()));
        }
        queueEachAsAStep(gameData, entry, abilities);
    }

    /**
     * Puts each of {@code abilities} on the stack as a step of this resolution, so one's target is chosen before the
     * next one's is asked.
     */
    private void queueEachAsAStep(GameData gameData, StackEntry entry, List<CardEffect> abilities) {
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, abilities);
            return;
        }
        for (CardEffect ability : abilities) {
            queueReflexiveAbilityEffectHandler.resolve(gameData, entry, ability);
        }
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves Dance of Many's enter-the-battlefield ability: create a token copy of the target nontoken
 * creature through the shared token-copy pipeline, so the controller orders the token creation
 * replacements (CR 616.1), and forge the mutual leaves-the-battlefield bond between the enchantment and
 * every token created (recorded in {@code GameData.sourceCreatedTokens} for the enchantment's
 * {@link com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect} trigger).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyAndLinkToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyAndLinkToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent sourceEnchantment = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent targetCreature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (targetCreature == null) {
            log.info("Game {} - Target creature no longer on battlefield, no token created", gameData.id);
            return;
        }

        // A Dance of Many that already left creates an unlinked token. Otherwise every token the ability
        // creates is "the token": the doubled copies and the tokens a replacement adds or substitutes
        // (Queen Allenal's Soldier, Jinnie Fay's Cats), since the creating effect's instructions apply to
        // all of them. The "when the token leaves, sacrifice this enchantment" trigger is the enchantment's
        // ability, not a copiable value of the token, so it rides on each token's permanent.
        boolean linked = sourceEnchantment != null;
        var copyOptions = new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), null, null, Map.of(),
                false, false, false, false, linked, false, null, Set.of(), false, List.of(), List.of(), false, false,
                new Fixed(1), false, Set.of(), false);
        UUID enchantmentId = linked ? sourceEnchantment.getId() : null;
        tokenCopySupport.createTokenCopiesWithTokenRider(gameData, entry, List.of(targetCreature.getCard()),
                entry.getControllerId(), copyOptions, linked ? token -> {
                    token.addPersistentTriggeredEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                            new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE_PARTNER));
                    token.setChosenPermanentId(enchantmentId);
                } : null);
        log.info("Game {} - Dance of Many creates a token copy of {}", gameData.id,
                targetCreature.getCard().getName());
    }
}

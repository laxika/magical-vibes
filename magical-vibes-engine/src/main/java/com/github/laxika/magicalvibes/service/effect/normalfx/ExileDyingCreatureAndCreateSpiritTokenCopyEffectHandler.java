package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateSpiritTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLinkedCardToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Hofri Ghostforge's death trigger by exiling the dying card and creating linked Spirit
 * copies of it through the shared token-copy pipeline, so the controller orders the token creation
 * replacements they control (CR 616.1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDyingCreatureAndCreateSpiritTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingCreatureAndCreateSpiritTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var hofriEffect = (ExileDyingCreatureAndCreateSpiritTokenCopyEffect) effect;
        UUID dyingCardId = hofriEffect.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        // The token copies the creature as it last existed on the battlefield (its copy values, e.g. a
        // Clone's copied creature), not the card it left in the graveyard.
        Permanent departed = gameData.departedPermanentSnapshots.get(dyingCardId);

        // A copy's battlefield card is not the physical card that went to the graveyard.
        UUID graveyardCardId = dyingCardId;
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, graveyardCardId);
        if (dyingCard == null && departed != null && departed.getOriginalCard() != null) {
            graveyardCardId = departed.getOriginalCard().getId();
            dyingCard = gameQueryService.findCardInGraveyardById(gameData, graveyardCardId);
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, graveyardCardId);
        if (ownerId == null || dyingCard == null) {
            return;
        }
        Card copySource = departed != null && departed.getCard() != null ? departed.getCard() : dyingCard;

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, graveyardCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);

        // "Except it's a Spirit in addition to its other types and has 'When this creature leaves the battlefield,
        // return the exiled card to its owner's graveyard'" are copy exceptions, but the link to the exiled card
        // belongs to the created tokens only: a copy of one of them has the ability and returns nothing.
        var copyOptions = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.SPIRIT), Set.of(), null, null, Map.of());
        UUID linkedCardId = graveyardCardId;
        tokenCopySupport.createTokenCopiesWithCopyException(gameData, entry, List.of(copySource), null,
                entry.getControllerId(), copyOptions,
                tokenCard -> tokenCard.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new ReturnLinkedCardToOwnerGraveyardEffect()),
                token -> token.setChosenPermanentId(linkedCardId));

        log.info("Game {} - {} creates a Spirit token copy of {}",
                gameData.id, entry.getCard().getName(), copySource.getName());
    }
}

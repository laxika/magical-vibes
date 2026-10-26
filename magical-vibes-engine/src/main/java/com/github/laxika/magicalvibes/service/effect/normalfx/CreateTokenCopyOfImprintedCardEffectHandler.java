package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates a token copy of the card imprinted on (exiled with) the source through the shared token-copy pipeline,
 * so the controller orders the token creation replacements they control (CR 616.1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfImprintedCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfImprintedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfImprintedCardEffect) effect;

        // Per rulings (Mimic Vat, Prototype Portal): if the source permanent has left the battlefield
        // by the time the ability resolves, the token is still created. The imprinted card reference
        // is preserved via entry.getCard() (a snapshot captured when the ability went on the stack).
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        Card imprintedCard = sourcePermanent != null
                ? gameData.getImprintedCard(sourcePermanent.getCard())
                : gameData.getImprintedCard(entry.getCard());
        // The source re-entering the battlefield (blink) drops its imprint, but an activated ability
        // that exiled a card as a cost still knows that card.
        if (imprintedCard == null && entry.getExiledCostCardId() != null) {
            var exiledAsCost = gameData.findExiledCard(entry.getExiledCostCardId());
            imprintedCard = exiledAsCost != null ? exiledAsCost.card() : null;
        }
        if (imprintedCard == null) {
            log.info("Game {} - No card imprinted on {}, no token created", gameData.id, entry.getCard().getName());
            return;
        }

        // A card that left exile is a new object, so it is no longer "a card exiled with" the source.
        if (gameData.findExiledCard(imprintedCard.getId()) == null) {
            log.info("Game {} - Imprinted card {} is no longer in exile, no token created",
                    gameData.id, imprintedCard.getName());
            return;
        }
        // A token copy of an instant or sorcery card cannot exist on the battlefield.
        if (!imprintedCard.hasType(CardType.CREATURE) && !imprintedCard.hasType(CardType.ARTIFACT)
                && !imprintedCard.hasType(CardType.ENCHANTMENT) && !imprintedCard.hasType(CardType.LAND)
                && !imprintedCard.hasType(CardType.PLANESWALKER) && !imprintedCard.hasType(CardType.BATTLE)) {
            return;
        }

        tokenCopySupport.createTokenCopies(gameData, entry, List.of(imprintedCard), null,
                entry.getControllerId(), toTokenCopyEffect(e));
    }

    /**
     * Expresses the imprint copy's exceptions and riders as a {@link CreateTokenCopyOfTargetPermanentEffect}.
     * "It gains haste" is granted to the token rather than copied, while a copied haste keyword is a copy
     * exception (CR 707.9b).
     */
    private static CreateTokenCopyOfTargetPermanentEffect toTokenCopyEffect(CreateTokenCopyOfImprintedCardEffect e) {
        return new CreateTokenCopyOfTargetPermanentEffect(e.additionalSubtypes(), e.additionalTypes(),
                e.powerOverride(), e.toughnessOverride(), Map.of(), e.grantHasteUntilEndOfTurn(),
                e.exileAtEndStep(), false, false, false, false, null,
                e.grantHaste() ? Set.of(Keyword.HASTE) : Set.of(), false,
                e.additionalEffects().isEmpty() ? Map.of() : Map.of(EffectSlot.STATIC, e.additionalEffects()),
                List.of(), false, false, new Fixed(1), false, Set.of(), false, List.of(), null, false,
                e.grantHasteUntilEndOfTurn(), null);
    }
}

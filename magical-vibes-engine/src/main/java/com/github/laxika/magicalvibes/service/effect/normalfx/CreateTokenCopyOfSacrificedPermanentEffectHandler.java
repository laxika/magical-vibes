package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Ashling-style copies of a sacrificed permanent using its last-known card. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSacrificedPermanentEffectHandler implements NormalEffectHandlerBean {

    private static final String TOKEN_SACRIFICE_COST = "{W}{U}{B}{R}{G}";

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSacrificedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfSacrificedPermanentEffect copyEffect =
                (CreateTokenCopyOfSacrificedPermanentEffect) effect;
        if (copyEffect.amount() <= 0) {
            return;
        }

        if (copyEffect.copiedCard() == null) {
            Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
            if (sacrificed == null) {
                return;
            }
            Permanent sourcePermanent = entry.getSourcePermanentId() == null
                    ? entry.getSourcePermanentSnapshot()
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            tokenCopySupport.createTokenCopies(gameData, entry,
                    Collections.nCopies(copyEffect.amount(), sacrificed.getCard()),
                    sourcePermanent, new CreateTokenCopyOfTargetPermanentEffect());
            return;
        }

        // One ordered creation event (CR 616.1). "The token gains haste" and the delayed sacrifice cover every token
        // the event creates, added ones such as Queen Allenal's Soldier included.
        UUID controllerId = entry.getControllerId();
        tokenCopySupport.createTokenCopiesWithTokenRider(gameData, entry,
                Collections.nCopies(copyEffect.amount(), copyEffect.copiedCard()), controllerId,
                new CreateTokenCopyOfTargetPermanentEffect(true, false).withHasteUntilEndOfTurn(),
                token -> gameData.queueDelayedAction(new SacrificePermanentAtControllerEndStepUnlessPays(
                        token.getId(), controllerId, entry.getCard(), new PayManaCost(TOKEN_SACRIFICE_COST), true)));
    }
}

package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        // A sacrifice trigger's "it" is the entry's triggering permanent (CR 603.10a); an activated ability's, the
        // permanent sacrificed to pay its cost.
        Permanent sacrificedByTrigger = entry.getTriggeringPermanentSnapshot();
        if (sacrificedByTrigger == null) {
            Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
            if (sacrificed == null) {
                return;
            }
            Permanent sourcePermanent = entry.getSourcePermanentId() == null
                    ? entry.getSourcePermanentSnapshot()
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            tokenCopySupport.createTokenCopies(gameData, entry,
                    Collections.nCopies(copyEffect.amount(), PermanentCopierService.cardToCopy(sacrificed)),
                    sourcePermanent, new CreateTokenCopyOfTargetPermanentEffect());
            return;
        }

        // One ordered creation event (CR 616.1). "The token gains haste" and the delayed sacrifice cover every token
        // the event creates, added ones such as Queen Allenal's Soldier included.
        UUID controllerId = entry.getControllerId();
        tokenCopySupport.createTokenCopiesWithTokenRider(gameData, entry,
                Collections.nCopies(copyEffect.amount(), PermanentCopierService.cardToCopy(sacrificedByTrigger)), controllerId,
                new CreateTokenCopyOfTargetPermanentEffect(true, false).withHasteUntilEndOfTurn(),
                token -> gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                        Triggers.endStep(ActivePlayer.YOU), controllerId, entry.getCard(),
                        token.getId(), token.getId(),
                        new ForcedCostOrElseEffect(new PayManaCost(TOKEN_SACRIFICE_COST),
                                new ArrayList<>(List.of(new SacrificeSelfEffect())), true))));
    }
}

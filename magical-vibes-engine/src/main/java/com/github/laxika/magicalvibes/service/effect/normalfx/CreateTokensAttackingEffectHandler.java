package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAttackingEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Creates the tokens as one ordered token creation event (CR 616.1), then every creature token of the event,
 * added tokens included, is attacking what its controller chooses (CR 508.4).
 */
@Component
@RequiredArgsConstructor
public class CreateTokensAttackingEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final ChooseAttackTargetsForCreatedTokensEffectHandler attackTargetsHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensAttackingEffect create = (CreateTokensAttackingEffect) effect;
        if (create.amount() <= 0) {
            return;
        }

        UUID controllerId = create.useTriggeringPermanentController()
                ? entry.getTriggeringPermanentControllerId() : entry.getControllerId();
        if (controllerId == null) {
            controllerId = entry.getControllerId();
        }
        int createdFromIndex = entry.getCreatedPermanentIds().size();
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, controllerId, create.tokenEffect(), create.amount(), entry.getCard().getSetCode()));
        attackTargetsHandler.createdTokensAttack(gameData, entry, controllerId, createdFromIndex,
                create.sacrificeAtEndStep());
    }
}

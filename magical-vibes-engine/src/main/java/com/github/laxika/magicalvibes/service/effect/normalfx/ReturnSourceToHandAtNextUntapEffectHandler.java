package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextUntapEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ReturnSourceToHandAtNextUntapEffect}: flags the source permanent so that
 * {@code UntapStepService} returns it to its owner's hand during its controller's next untap
 * step. Nothing happens if the source is no longer on the battlefield. Mana abilities apply this
 * rider inline instead (see {@code ActivatedAbilityExecutionService.doResolveManaAbility}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceToHandAtNextUntapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceToHandAtNextUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        source.setReturnToHandAtNextUntap(true);
        log.info("Game {} - {} scheduled to return to hand at next untap", gameData.id, entry.getCard().getName());
    }
}

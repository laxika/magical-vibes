package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Specializes the source permanent: a runtime copy of its card, its abilities cleared, gets the face the card writes
 * for the chosen color and replaces the card, and the face's "when this creature specializes" ability goes on the
 * stack. A source that left the battlefield, has specialized, or became another card since the ability was
 * activated doesn't specialize.
 */
@Component
@RequiredArgsConstructor
public class SpecializeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeEffect specialize = (SpecializeEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.getCard().getName().equals(entry.getCard().getName())) {
            return;
        }

        Card face = source.getCard().createRuntimeCopy();
        face.clearRulesTextAndAbilities();
        List<CardEffect> whenSpecializes = specialize.faces().writeFace(face, specialize.color());
        source.exchangeCard(face);
        if (whenSpecializes.isEmpty()) {
            return;
        }
        // "When this creature specializes" is an ability of the specialized face (CR 603.2), put on the stack by the
        // stack door, which chooses its targets as it goes there (CR 603.3d).
        triggeredAbilityStackService.putMade(gameData, TriggeredAbilityStackService.madeAbility(face,
                entry.getControllerId(), face.getName() + "'s ability", whenSpecializes, 0, source.getId()));
    }
}

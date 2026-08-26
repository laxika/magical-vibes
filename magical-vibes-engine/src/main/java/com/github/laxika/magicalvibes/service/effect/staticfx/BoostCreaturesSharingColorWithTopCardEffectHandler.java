package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesSharingColorWithTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BoostCreaturesSharingColorWithTopCardEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesSharingColorWithTopCardEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!support.matchesCreatureScope(context, GrantScope.ALL_OWN_CREATURES, null)) {
            return;
        }

        if (context.sourceControllerId() == null) {
            return;
        }
        List<Card> library = context.gameData().playerDecks.get(context.sourceControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }
        Card topCard = library.getFirst();
        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        Set<CardColor> topCardColors = gameQueryService.getEffectiveCardColors(context.gameData(), topCard);
        if (topCardColors.isEmpty()) {
            return;
        }

        Set<CardColor> targetColors = effectiveTargetColors(context);
        if (!intersects(targetColors, topCardColors)) {
            return;
        }

        BoostCreaturesSharingColorWithTopCardEffect boost =
                (BoostCreaturesSharingColorWithTopCardEffect) effect;
        accumulator.addPower(boost.powerBoost());
        accumulator.addToughness(boost.toughnessBoost());
    }

    private Set<CardColor> effectiveTargetColors(StaticEffectContext context) {
        CharacteristicState layered = LayerSystemService.activeStateFor(context.target().getId());
        if (layered != null) {
            return layered.getColors();
        }
        return gameQueryService.getEffectiveColors(context.gameData(), context.target());
    }

    private boolean intersects(Set<CardColor> first, Set<CardColor> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        EnumSet<CardColor> intersection = EnumSet.copyOf(first);
        intersection.retainAll(second);
        return !intersection.isEmpty();
    }
}

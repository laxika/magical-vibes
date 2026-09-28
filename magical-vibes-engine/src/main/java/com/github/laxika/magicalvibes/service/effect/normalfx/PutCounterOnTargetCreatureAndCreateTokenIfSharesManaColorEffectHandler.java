package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect) effect;
        Permanent target = entry.getTargetId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target, CounterType.PLUS_ONE_PLUS_ONE, 1);

        if (sharesColor(gameQueryService.getEffectiveColors(gameData, target), e.producedManaColors())) {
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), e.token(), entry.getCard().getSetCode()));
        }
    }

    private boolean sharesColor(Set<CardColor> creatureColors, Set<ManaColor> producedManaColors) {
        return producedManaColors.stream().anyMatch(manaColor -> switch (manaColor) {
            case WHITE -> creatureColors.contains(CardColor.WHITE);
            case BLUE -> creatureColors.contains(CardColor.BLUE);
            case BLACK -> creatureColors.contains(CardColor.BLACK);
            case RED -> creatureColors.contains(CardColor.RED);
            case GREEN -> creatureColors.contains(CardColor.GREEN);
            case COLORLESS -> false;
        });
    }
}

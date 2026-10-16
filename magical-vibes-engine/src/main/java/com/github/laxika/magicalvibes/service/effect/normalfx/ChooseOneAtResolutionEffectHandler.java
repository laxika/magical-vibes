package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a modal whose choice is intentionally deferred until the ability resolves. Per CR 608.2d
 * the player can't choose an impossible option, so a mode that starts by removing counters from the
 * source is not offered when the source doesn't have them.
 */
@Component
@RequiredArgsConstructor
public class ChooseOneAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = withoutImpossibleCounterRemovals(gameData, entry, ((ChooseOneAtResolutionEffect) effect).choice());
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                choice, false, entry.getSourcePermanentId());
    }

    private ChooseOneEffect withoutImpossibleCounterRemovals(GameData gameData, StackEntry entry,
                                                              ChooseOneEffect choice) {
        if (choice.choicesRequired() != 1 || choice.choicesMax() != 1 || entry.getSourcePermanentId() == null) {
            return choice;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        var legal = choice.options().stream()
                .filter(option -> option.effects().stream().noneMatch(optionEffect ->
                        optionEffect instanceof RemoveCounterFromSourceThenEffect removal
                                && removal.counterType() != CounterType.ANY
                                && (source == null || source.getCounterCount(removal.counterType()) < removal.count())))
                .toList();
        if (legal.isEmpty() || legal.size() == choice.options().size()) {
            return choice;
        }
        return new ChooseOneEffect(legal, choice.optional(), 1, 1);
    }
}

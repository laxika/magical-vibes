package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a modal whose choice is intentionally deferred until the ability resolves. Per CR 608.2d
 * the player can't choose an impossible option: a mode that starts by removing counters from the
 * source is not offered when the source doesn't have them, a mode that is only "sacrifice …" when you
 * control none that can be sacrificed, and a mode that is only "discard …" when your hand is empty
 * ("sacrifice an artifact or discard a card", "sacrifice a permanent unless you discard a card"). When no mode is
 * possible, every mode is offered; but a choice made only of such sacrifices and discards does nothing then, and
 * resolves its only possible mode without a prompt.
 */
@Component
@RequiredArgsConstructor
public class ChooseOneAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOneAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseOneEffect offered = ((ChooseOneAtResolutionEffect) effect).choice();
        if (onlySacrificesOrDiscards(offered) && offered.options().stream()
                .allMatch(option -> impossible(gameData, entry, option))) {
            gameLogService.append(gameData, GameLog.textCardText(gameData.playerIdToName.get(entry.getControllerId())
                    + " can do none of the options of ", entry.getCard(), "."));
            return;
        }
        var choice = withoutImpossibleOptions(gameData, entry, offered);
        if (onlySacrificesOrDiscards(offered) && choice.options().size() == 1) {
            // The only possible option leaves nothing to choose: it resolves as the next steps.
            ChooseOneEffect.ChooseOneOption only = choice.options().getFirst();
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, only.effects());
            gameLogService.append(gameData, GameLog.textCardText(gameData.playerIdToName.get(entry.getControllerId())
                    + " can only \"" + only.label() + "\" for ", entry.getCard(), "."));
            return;
        }
        playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                choice, entry.getSourcePermanentId());
    }

    private ChooseOneEffect withoutImpossibleOptions(GameData gameData, StackEntry entry, ChooseOneEffect choice) {
        if (choice.choicesRequired() != 1 || choice.choicesMax() != 1) {
            return choice;
        }
        var legal = choice.options().stream()
                .filter(option -> !impossible(gameData, entry, option))
                .toList();
        if (legal.isEmpty() || legal.size() == choice.options().size()) {
            return choice;
        }
        return new ChooseOneEffect(legal, choice.optional(), 1, 1);
    }

    /**
     * Whether the choice is "sacrifice … or discard …" of yours: every option is one such action, so an option that
     * can't be done does nothing, the only possible one leaves nothing to choose, and none possible leaves nothing to do.
     */
    private static boolean onlySacrificesOrDiscards(ChooseOneEffect choice) {
        return choice.choicesRequired() == 1 && choice.choicesMax() == 1
                && choice.options().stream().allMatch(option -> option.effects().size() == 1
                && isOwnSacrificeOrDiscard(option.effects().getFirst()));
    }

    private boolean impossible(GameData gameData, StackEntry entry, ChooseOneEffect.ChooseOneOption option) {
        List<CardEffect> effects = option.effects();
        if (effects.size() == 1 && isOwnSacrificeOrDiscard(effects.getFirst())) {
            return switch (effects.getFirst()) {
                case SacrificePermanentsEffect sacrifice -> !sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                        gameData, entry, sacrifice, entry.getControllerId());
                default -> gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty();
            };
        }
        return effects.stream().anyMatch(optionEffect -> optionEffect instanceof RemoveCounterFromSourceThenEffect removal
                && removal.counterType() != CounterType.ANY
                && entry.getSourcePermanentId() != null
                && lacksCounters(gameData, entry, removal));
    }

    private boolean lacksCounters(GameData gameData, StackEntry entry, RemoveCounterFromSourceThenEffect removal) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source == null || source.getCounterCount(removal.counterType()) < removal.count();
    }

    private static boolean isOwnSacrificeOrDiscard(CardEffect effect) {
        return effect instanceof SacrificePermanentsEffect sacrifice && sacrifice.player() instanceof PlayerSubject.You
                || effect instanceof DiscardEffect discard && discard.player() instanceof PlayerSubject.You;
    }
}

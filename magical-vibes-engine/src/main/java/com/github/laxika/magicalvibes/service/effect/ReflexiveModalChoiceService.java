package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The mode route of a reflexive ability a handler builds ("when you do, choose two —", CR 603.12): a modal
 * ability's modes, and then their targets, are chosen as it is put on the stack (CR 700.2b), before anyone receives
 * priority. Every handler that routes a reflexive ability asks here, so the modes are chosen the same way whichever
 * handler made the ability.
 */
@Service
@RequiredArgsConstructor
public class ReflexiveModalChoiceService {

    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /**
     * Begins the mode choice of the reflexive ability {@code record} is the plain entry of, when its one effect
     * {@code effect} is modal. "Choose up to that many" ({@link TriggeredModalEffect#maximumChoices()}) is read
     * against the record: its X, its event. With no mode to choose the ability is removed (CR 700.2b). Returns
     * whether the ability is modal; {@code false} leaves the other routes to the caller.
     */
    public boolean beginModeChoice(GameData gameData, StackEntry record, CardEffect effect) {
        ChooseOneEffect modes = TriggeredModalEffect.modesOf(effect);
        if (modes == null) {
            return false;
        }
        DynamicAmount maximum = TriggeredModalEffect.maximumModesOf(effect);
        if (maximum != null) {
            Permanent source = record.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, record.getSourcePermanentId());
            if (source == null) {
                source = record.getSourcePermanentSnapshot();
            }
            int most = Math.min(modes.choicesMax(), Math.max(0, amountEvaluationService.evaluate(
                    gameData, maximum, AmountContext.forStackEntry(record, source))));
            if (most == 0) {
                gameLogService.append(gameData, GameLog.cardThen(record.getCard(),
                        "'s reflexive ability has no modes to choose."));
                return true;
            }
            modes = modes.withChoiceCount(Math.min(modes.choicesRequired(), most), most);
        }
        playerInputService.beginTriggeredModalChoice(gameData, record.getControllerId(), record.getCard(), modes,
                record.getSourcePermanentId());
        return true;
    }
}

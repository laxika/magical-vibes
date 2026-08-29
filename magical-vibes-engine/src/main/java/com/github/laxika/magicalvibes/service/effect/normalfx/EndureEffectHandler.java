package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves the endure keyword action and its resolution-time choice. */
@Component
@RequiredArgsConstructor
public class EndureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EndureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EndureEffect endure = (EndureEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        UUID enduringPermanentId = endure.target() == EndureEffect.Target.TRIGGERING_PERMANENT
                ? entry.getTargetId()
                : entry.getSourcePermanentId();
        int amount = amountEvaluationService.evaluate(gameData, endure.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        Permanent enduringPermanent = enduringPermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, enduringPermanentId);

        CreateTokenEffect spirit = new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Spirit",
                amount,
                amount,
                CardColor.WHITE,
                null,
                List.of(CardSubtype.SPIRIT),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());

        if (enduringPermanent == null
                || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, enduringPermanent)) {
            insertAfterCurrent(entry, effect, spirit);
            return;
        }

        String counterLabel = "Put " + amount + " +1/+1 "
                + (amount == 1 ? "counter" : "counters") + " on this permanent";
        String tokenLabel = "Create a " + amount + "/" + amount
                + " white Spirit creature token";
        ChooseOneEffect choice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(counterLabel,
                        endure.target() == EndureEffect.Target.TRIGGERING_PERMANENT
                                ? new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, amount)
                                : new PutCountersOnSourceEffect(1, 1, amount)),
                new ChooseOneEffect.ChooseOneOption(tokenLabel, spirit)
        ));
        playerInputService.beginChooseModeChoice(
                gameData,
                entry.getControllerId(),
                entry.getCard(),
                choice,
                false,
                entry.getSourcePermanentId());
    }

    private void insertAfterCurrent(StackEntry entry, CardEffect current, CardEffect followUp) {
        int effectIndex = entry.getEffectsToResolve().indexOf(current);
        if (effectIndex < 0) {
            throw new IllegalStateException("EndureEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(followUp));
    }
}

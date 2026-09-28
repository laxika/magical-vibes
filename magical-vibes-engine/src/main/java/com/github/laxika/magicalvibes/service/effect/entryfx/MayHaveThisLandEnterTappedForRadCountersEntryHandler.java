package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayHaveThisLandEnterTappedForRadCountersEffect;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MayHaveThisLandEnterTappedForRadCountersEntryHandler implements EntryReplacementHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayHaveThisLandEnterTappedForRadCountersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        MayHaveThisLandEnterTappedForRadCountersEffect radEffect =
                (MayHaveThisLandEnterTappedForRadCountersEffect) effect;
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                enteringPermanent.getCard(),
                controllerId,
                List.of(radEffect),
                enteringPermanent.getCard().getName() + " — Have it enter tapped and get "
                        + radEffect.radCounterCount() + " rad counters?",
                null,
                null,
                enteringPermanent.getId()));
        playerInputService.processNextMayAbility(gameData);
    }
}

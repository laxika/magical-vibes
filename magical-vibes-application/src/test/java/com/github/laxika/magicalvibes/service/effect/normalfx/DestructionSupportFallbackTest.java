package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DestructionSupportFallbackTest {

    @Mock
    private BounceSupport bounceSupport;
    @Mock
    private EnergyCountersEffectHandler energyCountersEffectHandler;
    @Mock
    private GameData gameData;
    @Mock
    private StackEntry entry;

    @InjectMocks
    private DestructionSupport destructionSupport;

    @Test
    void dispatchesSelfReturnAndEnergyFallbacks() {
        ReturnToHandEffect returnToHand = ReturnToHandEffect.self();
        EnergyCountersEffect energy = new EnergyCountersEffect(1);
        ForcedCostOrElseEffect effect = new ForcedCostOrElseEffect(null, List.of(returnToHand, energy));

        destructionSupport.resolveForcedCostElseEffects(gameData, entry, effect);

        verify(bounceSupport).applyReturnSelfToHand(gameData, entry);
        verify(energyCountersEffectHandler).resolve(gameData, entry, energy);
    }
}

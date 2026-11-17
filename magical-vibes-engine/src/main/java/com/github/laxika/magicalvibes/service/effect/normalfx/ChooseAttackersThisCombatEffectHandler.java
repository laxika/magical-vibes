package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.DeclarationReplacementEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAttackersThisCombatEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseAttackersThisCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseAttackersThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(), null, controllerId,
                new DeclarationReplacementEffect(DeclarationReplacementEffect.Kind.CONTROLLER_CHOOSES_ATTACKERS,
                        entry.getCard()), null, Duration.UNTIL_END_OF_TURN));
        log.info("Game {} - {} hands attacker declaration to {} for this combat",
                gameData.id, entry.getCard().getName(), controllerId);
    }
}

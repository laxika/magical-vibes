package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseThisActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Records on the source permanent that it has lost the printed activated ability that just resolved. */
@Component
@RequiredArgsConstructor
public class LoseThisActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseThisActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                        .filter(p -> p.getCard() == entry.getCard()).findFirst().orElse(null);
        if (source == null) {
            return;
        }
        List<ActivatedAbility> printed = source.getCard().getActivatedAbilities();
        for (int i = 0; i < printed.size(); i++) {
            if (printed.get(i).getEffects().contains(effect)) {
                source.getLostPrintedActivatedAbilityIndices().add(i);
            }
        }
    }
}

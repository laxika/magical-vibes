package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a "you get N rad counters" rider on a mana ability (Harold and Bob, First Numens): the same replacements,
 * "players can't get counters" check and "you put counters" event as {@code GiveRadCountersEffectHandler}. Only a
 * {@code you()} subject reaches a mana ability.
 */
@Component
public class GiveRadCountersManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    public GiveRadCountersManaAbilityEffectHandler(AmountEvaluationService amountEvaluationService,
                                                   GameQueryService gameQueryService,
                                                   LifeSupport lifeSupport) {
        this.amountEvaluationService = amountEvaluationService;
        this.gameQueryService = gameQueryService;
        this.lifeSupport = lifeSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        GiveRadCountersEffect radCounters = (GiveRadCountersEffect) effect;
        if (!(radCounters.player() instanceof PlayerSubject.You)) {
            return;
        }
        int amount = amountEvaluationService.evaluate(gameData, radCounters.amount(),
                AmountContext.forManaAbility(permanent, playerId));
        if (amount <= 0) {
            return;
        }
        int placed = gameQueryService.replaceRadCounters(gameData, playerId, amount, playerId);
        lifeSupport.applyRadCounters(gameData, playerId, placed, permanent.getCard().getName(), playerId);
    }
}

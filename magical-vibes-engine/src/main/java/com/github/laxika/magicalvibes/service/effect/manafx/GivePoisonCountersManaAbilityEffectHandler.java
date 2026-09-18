package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a controller poison-counter rider on a mana ability. */
@Component
public class GivePoisonCountersManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final LifeSupport lifeSupport;
    private final AmountEvaluationService amountEvaluationService;

    public GivePoisonCountersManaAbilityEffectHandler(LifeSupport lifeSupport,
                                                       AmountEvaluationService amountEvaluationService) {
        this.lifeSupport = lifeSupport;
        this.amountEvaluationService = amountEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GivePoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        GivePoisonCountersEffect poison = (GivePoisonCountersEffect) effect;
        if (poison.recipient() != PoisonRecipient.CONTROLLER) return;

        int amount = amountEvaluationService.evaluate(gameData, poison.amount(),
                AmountContext.forManaAbility(permanent, playerId));
        lifeSupport.applyPoisonCounters(gameData, playerId, amount,
                permanent.getCard().getName(), playerId);
    }
}

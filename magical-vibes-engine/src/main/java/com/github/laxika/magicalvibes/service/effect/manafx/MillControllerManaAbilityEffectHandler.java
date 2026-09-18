package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a controller mill rider on a mana ability ("When you do, mill N cards"). */
@Component
public class MillControllerManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardService graveyardService;

    public MillControllerManaAbilityEffectHandler(AmountEvaluationService amountEvaluationService,
                                                  GraveyardService graveyardService) {
        this.amountEvaluationService = amountEvaluationService;
        this.graveyardService = graveyardService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        MillEffect mill = (MillEffect) effect;
        if (mill.recipient() != MillRecipient.CONTROLLER) {
            return;
        }

        int count = amountEvaluationService.evaluate(gameData, mill.count(),
                AmountContext.forManaAbility(permanent, playerId));
        graveyardService.resolveMillPlayer(gameData, playerId, Math.max(0, count));
    }
}

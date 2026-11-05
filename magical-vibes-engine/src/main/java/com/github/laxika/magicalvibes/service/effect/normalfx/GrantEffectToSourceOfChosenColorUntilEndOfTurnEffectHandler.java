package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;

@Component
@RequiredArgsConstructor
public class GrantEffectToSourceOfChosenColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect) effect;
        CardColor chosenColor = gameData.chosenSpellColor;
        gameData.chosenSpellColor = null;
        if (chosenColor == null || entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        // "Whenever this creature becomes blocked by a creature of that color this turn": the chosen color is the
        // trigger's filter on the blocking creature.
        TriggeredAbility granted = (TriggeredAbility) grant.grantedAbility();
        CombatTrigger trigger = (CombatTrigger) granted.conditions().getFirst().trigger();
        source.addTemporaryAbility(new TriggeredAbility(
                trigger.withFilter(new PermanentColorInPredicate(Set.of(chosenColor))),
                granted.options(), granted.effects()));
        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" gains a temporary ability until end of turn.")
                .build());
    }
}

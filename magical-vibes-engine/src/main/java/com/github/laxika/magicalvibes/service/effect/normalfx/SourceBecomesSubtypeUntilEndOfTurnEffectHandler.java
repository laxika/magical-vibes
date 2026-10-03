package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SourceBecomesSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceBecomesSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SourceBecomesSubtypeUntilEndOfTurnEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }
        if (!e.overriding()) {
            for (var subtype : e.subtypes()) {
                gameData.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                        entry.getControllerId(), new com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect(
                                subtype, com.github.laxika.magicalvibes.model.effect.GrantScope.TARGET),
                        self.getId(), null, null, com.github.laxika.magicalvibes.model.effect.EffectDuration.UNTIL_END_OF_TURN, 0));
            }
        } else if (e.subtypes().size() == 1) {
            self.setTransientCreatureTypeOverride(e.subtype());
            self.getTransientCreatureTypeOverrides().clear();
        } else {
            self.setTransientCreatureTypeOverride(null);
            self.getTransientCreatureTypeOverrides().clear();
            self.getTransientCreatureTypeOverrides().addAll(e.subtypes());
        }
        String typeNames = e.subtypes().stream()
                .map(subtype -> subtype.getDisplayName())
                .reduce((left, right) -> left + " and " + right)
                .orElseThrow();
        gameLogService.append(gameData, GameLog.text(self.getCard().getName()
                + " becomes a " + typeNames + " until end of turn."));
    }
}

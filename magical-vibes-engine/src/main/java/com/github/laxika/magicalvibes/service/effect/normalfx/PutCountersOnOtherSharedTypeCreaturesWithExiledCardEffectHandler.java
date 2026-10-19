package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** Resolves the reflexive counter ability queued after a shared-creature-type graveyard exile. */
@Component
@RequiredArgsConstructor
public class PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect) effect;
        Card exiled = entry.getExiledCostCardSnapshot();
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (exiled == null || battlefield == null) {
            return;
        }
        Permanent exiledCardAsPermanent = new Permanent(exiled);
        List<Permanent> matching = battlefield.stream()
                .filter(permanent -> !Objects.equals(entry.getSourcePermanentId(), permanent.getId())
                        && !Objects.equals(entry.getCard().getId(), permanent.getCard().getId()))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> gameQueryService.shareCreatureType(gameData, exiledCardAsPermanent, permanent))
                .toList();
        for (Permanent permanent : matching) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, permanent, e.counterType(), e.counterCount());
        }
    }
}

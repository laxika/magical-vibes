package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToChosenCreatureOrPlaneswalkerThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Gideon's Sacrifice's choice and installs its turn-scoped damage redirect. */
@Component
@RequiredArgsConstructor
public class RedirectYourDamageToChosenCreatureOrPlaneswalkerThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectYourDamageToChosenCreatureOrPlaneswalkerThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        List<UUID> validIds = new ArrayList<>();
        List<com.github.laxika.magicalvibes.model.Permanent> battlefield =
                gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (var permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        || gameQueryService.isPlaneswalker(gameData, permanent)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) return;

        playerInputService.beginMultiPermanentChoice(
                gameData,
                controllerId,
                validIds,
                1,
                new MultiPermanentChoiceContext.RedirectDamageToChosenPermanent(
                        controllerId, entry.getCard().getName()),
                entry.getCard().getName() + " — choose a creature or planeswalker you control.");
    }
}

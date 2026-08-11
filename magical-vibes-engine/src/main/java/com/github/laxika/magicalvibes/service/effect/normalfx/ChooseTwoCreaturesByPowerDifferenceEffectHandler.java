package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseTwoCreaturesByPowerDifferenceEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseTwoCreaturesByPowerDifferenceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseTwoCreaturesByPowerDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureIds = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();

        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() < 2) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), creatureIds, 2,
                new MultiPermanentChoiceContext.ChooseTwoCreaturesByPowerDifference(),
                "Choose exactly two creatures you control.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        List<Permanent> chosen = permanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .toList();
        if (chosen.isEmpty()) {
            return;
        }

        int x = chosen.size() == 2
                ? Math.abs(gameQueryService.getEffectivePower(gameData, chosen.get(0))
                        - gameQueryService.getEffectivePower(gameData, chosen.get(1)))
                : 0;
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), x);

        GrantKeywordEffect trampleGrant = new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET);
        String sourceCardName = entry.getCard() != null ? entry.getCard().getName() : "Spry and Mighty";
        for (Permanent permanent : chosen) {
            permanent.setPowerModifier(permanent.getPowerModifier() + x);
            permanent.setToughnessModifier(permanent.getToughnessModifier() + x);
            permanent.getGrantedKeywords().addAll(trampleGrant.keywords());
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceCardName, null,
                    entry.getControllerId(), trampleGrant, permanent.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}

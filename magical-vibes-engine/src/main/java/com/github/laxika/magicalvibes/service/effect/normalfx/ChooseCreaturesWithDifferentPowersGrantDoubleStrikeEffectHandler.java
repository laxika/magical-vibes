package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE;

/** Resolves Sigarda's Vanguard's non-targeted distinct-power creature choice. */
@Component
@RequiredArgsConstructor
public class ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureIds = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureIds.add(permanent.getId());
            }
        });

        if (creatureIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                creatureIds,
                creatureIds.size(),
                new MultiPermanentChoiceContext.ChooseCreaturesWithDifferentPowersGrantDoubleStrike(),
                "Choose any number of creatures with different powers.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        List<Permanent> chosen = new ArrayList<>();
        Set<Integer> powers = new HashSet<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)
                    || !powers.add(gameQueryService.getEffectivePower(gameData, permanent))) {
                return;
            }
            chosen.add(permanent);
        }

        GrantKeywordEffect grant = new GrantKeywordEffect(DOUBLE_STRIKE, GrantScope.TARGET);
        for (Permanent permanent : chosen) {
            if (gameQueryService.cantHaveOrGainKeyword(gameData, permanent, DOUBLE_STRIKE)) {
                continue;
            }
            permanent.getGrantedKeywords().add(DOUBLE_STRIKE);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                    entry.getControllerId(), grant, permanent.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }
}

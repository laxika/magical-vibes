package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDrawAndMayPutPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SacrificeAnotherCreatureDrawAndMayPutPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnotherCreatureDrawAndMayPutPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (SacrificeAnotherCreatureDrawAndMayPutPermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();
        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && !permanent.getCard().getId().equals(sourceCardId)) {
                    creatureIds.add(permanent.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no other creature to sacrifice."));
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                resolveAfterChoice(gameData, entry, creature, typedEffect);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeAnotherCreatureDrawAndMayPutPermanent(
                        controllerId, entry.getCard(), typedEffect));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                entry.getCard().getName() + " — Choose another creature to sacrifice.");
    }

    public void resolveAfterChoice(GameData gameData, Permanent creature,
                                   SacrificeAnotherCreatureDrawAndMayPutPermanentEffect effect) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending resolution for sacrifice choice");
        }
        resolveAfterChoice(gameData, entry, creature, effect);
    }

    private void resolveAfterChoice(GameData gameData, StackEntry entry, Permanent creature,
                                    SacrificeAnotherCreatureDrawAndMayPutPermanentEffect effect) {
        int manaValue = creature.getCard().getManaValue();
        destructionSupport.sacrificeAndLog(gameData, creature, entry.getControllerId());

        CardAllOfPredicate permanentFilter = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardMaxManaValuePredicate(manaValue)));
        PutCardToBattlefieldEffect putPermanent = new PutCardToBattlefieldEffect(
                permanentFilter, "permanent");
        List<com.github.laxika.magicalvibes.model.effect.CardEffect> followUps = List.of(
                new DrawCardEffect(manaValue),
                new MayEffect(putPermanent,
                        "You may put a permanent card with mana value " + manaValue
                                + " or less from your hand onto the battlefield."));

        int effectIndex = findEffectIndex(entry, effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Sacrifice effect is not on its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }

    private int findEffectIndex(StackEntry entry,
                                 SacrificeAnotherCreatureDrawAndMayPutPermanentEffect effect) {
        List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            CardEffect candidate = effects.get(i);
            if (candidate == effect
                    || candidate instanceof MayEffect may && may.wrapped() == effect) {
                return i;
            }
        }
        return -1;
    }
}

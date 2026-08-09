package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureThenMassDamageEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Rupture's resolution-time creature sacrifice and subsequent mass damage. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeCreatureThenMassDamageEqualToPowerEffectHandler implements NormalEffectHandlerBean {

    private static final PermanentNotPredicate WITHOUT_FLYING =
            new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final MassDamageEffectHandler massDamageEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatureThenMassDamageEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " controls no creature to sacrifice for ", entry.getCard(), "."));
            log.info("Game {} - {} has no creature to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower(
                        controllerId, entry.getCard()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose a creature to sacrifice.");

        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " is choosing a creature to sacrifice for ", entry.getCard(), "."));
        log.info("Game {} - {} choosing a creature to sacrifice for {}",
                gameData.id, playerName, entry.getCard().getName());
    }

    public void resolveAfterChoice(GameData gameData, Permanent chosen,
                                   PermanentChoiceContext.SacrificeCreatureThenMassDamageEqualToPower context) {
        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, chosen));
        permanentRemovalService.removePermanentToGraveyard(gameData, chosen);

        String playerName = gameData.playerIdToName.get(context.controllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " sacrifices ", chosen.getCard(), "."));
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                chosen.getCard().getName(), context.sourceCard().getName());

        if (power > 0) {
            MassDamageEffect massDamage = new MassDamageEffect(new Fixed(power), true, false, WITHOUT_FLYING);
            StackEntry entry = new StackEntry(
                    StackEntryType.SORCERY_SPELL,
                    context.sourceCard(),
                    context.controllerId(),
                    context.sourceCard().getName(),
                    List.of(massDamage));
            massDamageEffectHandler.resolve(gameData, entry, massDamage);
        }
    }
}

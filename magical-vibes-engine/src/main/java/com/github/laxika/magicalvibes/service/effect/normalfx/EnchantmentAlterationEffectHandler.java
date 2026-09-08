package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantmentAlterationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnchantmentAlterationEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantmentAlterationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (aura == null || !aura.getCard().isAura() || !aura.isAttached()) {
            fizzle(gameData, entry, "target is no longer an Aura attached to a creature or land");
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (host == null || (!gameQueryService.isCreature(gameData, host)
                && !gameQueryService.isLand(gameData, host))) {
            fizzle(gameData, entry, "the Aura is no longer attached to a creature or land");
            return;
        }

        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        List<UUID> validRecipientIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, candidate) -> {
            if (candidate.getId().equals(aura.getId()) || candidate.getId().equals(host.getId())) {
                return;
            }
            if (!sharesCreatureOrLandType(gameData, host, candidate)) {
                return;
            }
            if (auraControllerId == null || !canMoveTo(gameData, aura, auraControllerId, candidate)) {
                return;
            }
            validRecipientIds.add(candidate.getId());
        });

        if (validRecipientIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(aura.getCard(), " stays attached to its current target (no other valid permanents)."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.EnchantmentAlteration(aura.getId(), host.getId()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validRecipientIds,
                "Attach " + aura.getCard().getName() + " to another permanent of the same type.");
    }

    private boolean sharesCreatureOrLandType(GameData gameData, Permanent first, Permanent second) {
        return gameQueryService.isCreature(gameData, first) && gameQueryService.isCreature(gameData, second)
                || gameQueryService.isLand(gameData, first) && gameQueryService.isLand(gameData, second);
    }

    private boolean canMoveTo(GameData gameData, Permanent aura, UUID auraControllerId, Permanent candidate) {
        return auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, candidate)
                && !gameQueryService.hasProtectionFromSource(gameData, candidate, aura);
    }

    private void fizzle(GameData gameData, StackEntry entry, String reason) {
        gameLogService.append(gameData,
                GameLog.builder().card(entry.getCard()).text("'s spell fizzles (" + reason + ").").build());
        log.info("Game {} - {} fizzles: {}", gameData.id, entry.getCard().getName(), reason);
    }
}

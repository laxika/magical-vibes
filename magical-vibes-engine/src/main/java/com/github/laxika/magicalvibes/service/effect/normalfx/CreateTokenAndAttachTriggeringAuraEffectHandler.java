package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndAttachTriggeringAuraEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Ajani's Chosen: creates the Cat token, then — when the enchantment that triggered the ability is
 * an Aura still on the battlefield that could legally enchant the token — offers its controller the
 * optional move via the shared attachment prompt ({@code pendingEquipmentAttach}), which is also
 * what Auriok Survivors uses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenAndAttachTriggeringAuraEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAndAttachTriggeringAuraEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenAndAttachTriggeringAuraEffect) effect;
        List<UUID> created = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(created);
        if (created.isEmpty() || entry.getTriggeringCardId() == null) {
            return;
        }

        Permanent token = gameQueryService.findPermanentById(gameData, created.getFirst());
        Permanent aura = findAuraByCardId(gameData, entry.getTriggeringCardId());
        if (token == null || aura == null || !aura.getCard().isAura()) {
            return;
        }
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (!auraAttachmentService.canEnchant(gameData, aura.getCard(), auraControllerId, token)) {
            return;
        }

        gameData.interaction.setPendingEquipmentAttach(aura.getId(), token.getId());
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                entry.getCard(), entry.getControllerId(), List.of(),
                entry.getCard().getName() + " — Attach " + aura.getCard().getName()
                        + " to the Cat token?"));
        log.info("Game {} - {} offers to move {} onto its token", gameData.id,
                entry.getCard().getName(), aura.getCard().getName());
    }

    private Permanent findAuraByCardId(GameData gameData, UUID cardId) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}

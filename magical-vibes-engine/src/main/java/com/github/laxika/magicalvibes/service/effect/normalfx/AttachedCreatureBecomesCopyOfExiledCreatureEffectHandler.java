package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraCopyService auraCopyService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachedCreatureBecomesCopyOfExiledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID attachedCreatureId = entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId() : entry.getTargetId();
        if (equipment == null || !equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                || attachedCreatureId == null
                || !attachedCreatureId.equals(equipment.getAttachedTo())) {
            return;
        }
        Permanent attached = gameQueryService.findPermanentById(gameData, attachedCreatureId);
        if (attached == null) {
            return;
        }

        List<ExiledCardEntry> eligible = eligibleCards(gameData, equipment.getId(), equipment.getCard().getId());
        if (eligible.isEmpty()) {
            return;
        }
        if (eligible.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.AssimilationAegisCopyChoice(
                    entry.getControllerId(), equipment.getId(), attached.getId(),
                    eligible.stream().map(exiled -> exiled.card().getId()).toList()));
            return;
        }

        auraCopyService.applyExiledCreatureCopy(gameData, equipment, attached, eligible.getFirst().card());
    }

    public static List<ExiledCardEntry> eligibleCards(GameData gameData, UUID equipmentId,
                                                       UUID equipmentCardId) {
        return gameData.getExiledWithPermanentEntries(equipmentId, equipmentCardId).stream()
                .filter(entry -> !entry.faceDown() && entry.card().hasType(CardType.CREATURE))
                .toList();
    }
}

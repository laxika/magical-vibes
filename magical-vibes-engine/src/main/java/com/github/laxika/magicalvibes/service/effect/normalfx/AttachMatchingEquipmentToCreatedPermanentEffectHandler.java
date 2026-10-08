package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachMatchingEquipmentToCreatedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachMatchingEquipmentToCreatedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, entry.getCreatedPermanentIds().getFirst());
        if (host == null || !gameQueryService.isCreature(gameData, host)) {
            return;
        }

        var attachmentFilter = ((AttachMatchingEquipmentToCreatedPermanentEffect) effect).equipmentFilter();
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourceControllerId(entry.getControllerId());
        List<Permanent> equipmentPermanents = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT)
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, attachmentFilter, context)) {
                equipmentPermanents.add(permanent);
            }
        });

        var attachmentEffect = (AttachMatchingEquipmentToCreatedPermanentEffect) effect;
        if (attachmentEffect.chooseCreatedPermanent() && !equipmentPermanents.isEmpty()) {
            var hosts = entry.getCreatedPermanentIds().stream()
                    .map(id -> gameQueryService.findPermanentById(gameData, id))
                    .filter(permanent -> permanent != null && gameQueryService.isCreature(gameData, permanent))
                    .filter(permanent -> equipmentPermanents.stream().anyMatch(equipment ->
                            equipSupport.canAttachEquipment(gameData, equipment, permanent)))
                    .map(Permanent::getId).toList();
            if (hosts.size() > 1) {
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), hosts,
                        new PermanentChoiceContext.AttachMatchingEquipmentToCreatedPermanent(
                                equipmentPermanents.stream().map(Permanent::getId).toList()),
                        "Choose a created creature to attach the Equipment to.");
                return;
            }
        }

        for (Permanent equipment : equipmentPermanents) {
            if (host.getId().equals(equipment.getAttachedTo())
                    || !equipSupport.canAttachEquipment(gameData, equipment, host)) {
                continue;
            }

            var oldAttachedTo = equipment.getAttachedTo();
            equipSupport.expireAttachedCopyEffects(gameData, equipment);
            equipment.setAttachedTo(host.getId());
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
            log.info("Game {} - {} attached to {}", gameData.id,
                    equipment.getCard().getName(), host.getCard().getName());
        }
    }
}

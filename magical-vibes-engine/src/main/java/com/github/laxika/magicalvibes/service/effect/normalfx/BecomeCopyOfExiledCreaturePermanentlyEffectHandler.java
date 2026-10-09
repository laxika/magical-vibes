package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies Curie's permanent copy of the creature exiled to pay its ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfExiledCreaturePermanentlyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfExiledCreaturePermanentlyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent exiledPermanent = chosenCostPermanent(gameData, entry);
        Card exiledCard = exiledPermanent == null ? null : exiledPermanent.getCard();
        if (exiledCard == null && entry.getSourcePermanentSnapshot() != null) {
            exiledCard = entry.getSourcePermanentSnapshot().getChosenExiledCard();
        }
        if (source == null || exiledCard == null) {
            return;
        }

        if (!exiledCard.hasType(CardType.CREATURE)) {
            return;
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, exiledCard, null, null, Set.of());
        BecomeCopyOfExiledCreaturePermanentlyEffect copyEffect =
                (BecomeCopyOfExiledCreaturePermanentlyEffect) effect;
        copyEffect.additionalSlotEffects().forEach((slot, effects) ->
                effects.forEach(cardEffect -> source.getCard().addEffect(slot, cardEffect)));

        log.info("Game {} - {} becomes a copy of {} permanently", gameData.id,
                originalName, exiledCard.getName());
    }

    private Permanent chosenCostPermanent(GameData gameData, StackEntry entry) {
        List<UUID> chosenIds = entry.getChosenCostPermanentIds();
        if (chosenIds == null || chosenIds.isEmpty()) {
            return null;
        }
        UUID chosenId = chosenIds.getFirst();
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
        if (chosen != null) {
            return chosen;
        }
        return entry.getChosenCostPermanentSnapshots().stream()
                .filter(snapshot -> chosenId.equals(snapshot.getId()))
                .findFirst()
                .orElse(null);
    }
}

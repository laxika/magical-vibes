package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CopyResolutionService implements EffectHandlerProvider {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(CopySpellEffect.class,
                (gd, entry, effect) -> resolveCopySpell(gd, entry));
    }

    void resolveCopySpell(GameData gameData, StackEntry entry) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Copy target no longer on stack", gameData.id);
            return;
        }

        // Create a copy Card with a new identity but same properties
        Card copyCard = createCopyCard(targetEntry.getCard());

        // Create a copy of the stack entry preserving all fields, with the copy's controller
        UUID copyControllerId = entry.getControllerId();
        StackEntry copyEntry = new StackEntry(
                targetEntry.getEntryType(),
                copyCard,
                copyControllerId,
                "Copy of " + targetEntry.getCard().getName(),
                new ArrayList<>(targetEntry.getEffectsToResolve()),
                targetEntry.getXValue(),
                targetEntry.getTargetPermanentId(),
                targetEntry.getSourcePermanentId(),
                targetEntry.getDamageAssignments(),
                targetEntry.getTargetZone(),
                targetEntry.getTargetCardIds() != null ? new ArrayList<>(targetEntry.getTargetCardIds()) : null
        );
        copyEntry.setCopy(true);

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + targetEntry.getCard().getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} copies {}", gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());

        // If the copy has a target, offer the controller a chance to choose new targets
        if (copyEntry.getTargetPermanentId() != null) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + targetEntry.getCard().getName() + "?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }

    private Card createCopyCard(Card original) {
        Card copy = new Card();

        copy.setName(original.getName());
        copy.setType(original.getType());
        copy.setManaCost(original.getManaCost());
        copy.setColor(original.getColor());
        copy.setSupertypes(original.getSupertypes());
        copy.setSubtypes(original.getSubtypes());
        copy.setCardText(original.getCardText());
        copy.setPower(original.getPower());
        copy.setToughness(original.getToughness());
        copy.setKeywords(original.getKeywords());
        copy.setNeedsTarget(original.isNeedsTarget());
        copy.setNeedsSpellTarget(original.isNeedsSpellTarget());
        copy.setTargetFilter(original.getTargetFilter());
        copy.setLoyalty(original.getLoyalty());
        copy.setXColorRestriction(original.getXColorRestriction());

        return copy;
    }
}

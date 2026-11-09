package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddManaForEachOtherLandWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Sasaya's Essence's triggered mana ability from the stack entry: one mana of a type the tapped land produced
 * for each other land the player who tapped it controls with the same name, counted as the ability resolves
 * (CR 608.2h). A land that produced several types lets the player choose each mana's type (its ruling: "any
 * combination").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaForEachOtherLandWithSameNameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaForEachOtherLandWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent tappedLand = TappedLandManaSupport.tappedLand(gameData, gameQueryService, entry);
        UUID recipientId = TappedLandManaSupport.manaRecipient(entry);
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (tappedLand == null || pool == null || !gameQueryService.isLand(gameData, tappedLand)) {
            return;
        }
        String tappedLandName = gameQueryService.getEffectiveName(gameData, tappedLand);
        int matchingLandCount = Math.toIntExact(gameData.playerBattlefields
                .getOrDefault(recipientId, List.of()).stream()
                .filter(other -> !other.getId().equals(tappedLand.getId()))
                .filter(other -> gameQueryService.isLand(gameData, other))
                .filter(other -> tappedLandName.equals(gameQueryService.getEffectiveName(gameData, other)))
                .count());
        List<ManaColor> choices = TappedLandManaSupport.producedManaTypes(entry, tappedLand).stream().sorted().toList();
        if (matchingLandCount == 0 || choices.isEmpty()) {
            return;
        }

        if (choices.size() == 1) {
            pool.add(choices.getFirst(), matchingLandCount);
        } else {
            for (int i = 0; i < matchingLandCount; i++) {
                StackEntry noFollowUp = new StackEntry(StackEntryType.TRIGGERED_ABILITY, entry.getCard(),
                        recipientId, entry.getDescription(), new ArrayList<>(), null, entry.getSourcePermanentId());
                noFollowUp.setNonTargeting(true);
                AnyColorManaChoiceSupport.beginOrQueueChoice(interactionHandlerRegistry, gameData,
                        new PendingInteraction.ColorChoice(recipientId, null, null,
                                new ChoiceContext.FixedManaColorThenEffectsChoice(recipientId, choices, 1, noFollowUp),
                                choices.stream().map(Enum::name).toList(),
                                "Choose a type of mana the land produced."));
            }
        }
        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + matchingLandCount
                + " additional mana of a type produced by " + tappedLand.getCard().getName() + "."));
        log.info("Game {} - {} adds {} additional mana for lands named {}", gameData.id, playerName,
                matchingLandCount, tappedLandName);
    }
}

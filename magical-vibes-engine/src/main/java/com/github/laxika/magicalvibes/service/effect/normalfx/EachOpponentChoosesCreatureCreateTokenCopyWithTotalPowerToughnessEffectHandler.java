package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Benthic Anomaly's per-opponent creature choices and modified token copy. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        beginNextOpponent(gameData, entry, apnapOpponents(gameData, entry.getControllerId()),
                List.of(), 0, 0);
    }

    private void beginNextOpponent(GameData gameData, StackEntry entry, List<UUID> remainingOpponentIds,
                                   List<UUID> chosenPermanentIds, int totalPower, int totalToughness) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);
        int power = totalPower;
        int toughness = totalToughness;

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, opponentId, ignored -> true);
            if (creatureIds.isEmpty()) {
                continue;
            }

            if (creatureIds.size() == 1) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
                if (creature != null) {
                    chosen.add(creature.getId());
                    power += gameQueryService.getEffectivePower(gameData, creature);
                    toughness += gameQueryService.getEffectiveToughness(gameData, creature);
                }
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesCreatureForTokenCopy context =
                    new PermanentChoiceContext.EachOpponentChoosesCreatureForTokenCopy(
                            entry, entry.getControllerId(), opponentId, remaining, chosen, power, toughness);
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds, context,
                    entry.getCard().getName() + " - choose a creature that opponent controls.");
            return;
        }

        chooseCopySource(gameData, entry, chosen, power, toughness);
    }

    public void completeCreatureChoice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.EachOpponentChoosesCreatureForTokenCopy context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Chosen permanent is no longer a creature controlled by that opponent");
        }

        List<UUID> chosenIds = new ArrayList<>(context.chosenPermanentIds());
        chosenIds.add(permanentId);
        beginNextOpponent(gameData, context.resolvingEntry(), context.remainingOpponentIds(), chosenIds,
                context.totalPower() + gameQueryService.getEffectivePower(gameData, chosen),
                context.totalToughness() + gameQueryService.getEffectiveToughness(gameData, chosen));
    }

    public void completeCopyChoice(GameData gameData, UUID permanentId,
                                   PermanentChoiceContext.ChooseBenthicAnomalyCopy context) {
        if (!context.chosenPermanentIds().contains(permanentId)) {
            throw new IllegalStateException("Chosen permanent was not selected for Benthic Anomaly");
        }
        createTokenCopy(gameData, context.resolvingEntry(), permanentId,
                context.totalPower(), context.totalToughness());
    }

    private void chooseCopySource(GameData gameData, StackEntry entry, List<UUID> chosenPermanentIds,
                                  int totalPower, int totalToughness) {
        if (chosenPermanentIds.isEmpty()) {
            return;
        }
        if (chosenPermanentIds.size() == 1) {
            createTokenCopy(gameData, entry, chosenPermanentIds.getFirst(), totalPower, totalToughness);
            return;
        }

        PermanentChoiceContext.ChooseBenthicAnomalyCopy context =
                new PermanentChoiceContext.ChooseBenthicAnomalyCopy(
                        entry, entry.getControllerId(), chosenPermanentIds, totalPower, totalToughness);
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), chosenPermanentIds, context,
                entry.getCard().getName() + " - choose one of those creatures to copy.");
    }

    private void createTokenCopy(GameData gameData, StackEntry entry, UUID sourcePermanentId,
                                 int totalPower, int totalToughness) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }
        CreateTokenCopyOfTargetPermanentEffect profile = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), totalPower, totalToughness, java.util.Map.of());
        tokenCopySupport.createTokenCopiesWithCopyException(gameData, entry, List.of(source.getCard()), null,
                entry.getControllerId(), profile, tokenCard -> {
                    tokenCard.setColor(null);
                    tokenCard.setColors(List.of());
                    tokenCard.setType(CardType.CREATURE);
                    tokenCard.setAdditionalTypes(Set.of());
                    tokenCard.setSubtypes(List.of(CardSubtype.ELDRAZI));
                });
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex >= 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}

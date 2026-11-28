package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachFightsADifferentOneEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resolves {@link EachFightsADifferentOneEffect}: the creatures of the smaller side are paired in turn, each with one
 * of the other side's the controller chooses ({@link PermanentChoiceContext.FightPairing}), unasked when one candidate
 * is left or every candidate left is alike (the same name, power, toughness, damage, controller and token status, so
 * the choice changes nothing); then every pair fights at once ({@link FightSupport#fightTogether}). The pairing ends the
 * step: the fights are its last action, made as the last choice is answered.
 */
@Component
@RequiredArgsConstructor
public class EachFightsADifferentOneEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final FightSupport fightSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachFightsADifferentOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var fight = (EachFightsADifferentOneEffect) effect;
        List<UUID> fighters = creatureIds(gameData,
                subjectResolutionService.permanents(gameData, entry, fight, fight.fighters()));
        List<UUID> opponents = new ArrayList<>(creatureIds(gameData,
                subjectResolutionService.permanents(gameData, entry, fight, fight.opponents())));
        opponents.removeAll(fighters);
        // For each creature of the smaller side, one of the other side's; the larger side's extra ones fight nothing.
        boolean choosingFighter = fighters.size() >= opponents.size();
        pairNext(gameData, entry, fight, choosingFighter, choosingFighter ? opponents : fighters,
                choosingFighter ? fighters : opponents, List.of(), List.of());
    }

    /** The controller's answer to {@code context}: {@code chosenId} is paired with the creature it was asked for. */
    public void completePairing(GameData gameData, UUID chosenId, PermanentChoiceContext.FightPairing context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a fight pairing");
        }
        if (!context.candidates().contains(chosenId)) {
            throw new IllegalStateException("Choose one of the offered creatures");
        }
        List<UUID> pairedFighters = new ArrayList<>(context.pairedFighters());
        List<UUID> pairedOpponents = new ArrayList<>(context.pairedOpponents());
        pairedFighters.add(context.choosingFighter() ? chosenId : context.askedFor());
        pairedOpponents.add(context.choosingFighter() ? context.askedFor() : chosenId);
        List<UUID> candidates = new ArrayList<>(context.candidates());
        candidates.remove(chosenId);
        pairNext(gameData, entry, context.effect(), context.choosingFighter(), context.pickers(), candidates,
                pairedFighters, pairedOpponents);
    }

    private void pairNext(GameData gameData, StackEntry entry, EachFightsADifferentOneEffect fight,
                          boolean choosingFighter, List<UUID> pickers, List<UUID> candidates,
                          List<UUID> pairedFighters, List<UUID> pairedOpponents) {
        List<UUID> fighters = new ArrayList<>(pairedFighters);
        List<UUID> opponents = new ArrayList<>(pairedOpponents);
        List<UUID> left = new ArrayList<>(candidates);
        for (int i = 0; i < pickers.size(); i++) {
            UUID picker = pickers.get(i);
            List<UUID> live = left.stream().filter(id -> isCreature(gameData, id)).toList();
            if (live.isEmpty()) {
                break;
            }
            if (!isCreature(gameData, picker)) {
                continue;
            }
            UUID chosen;
            if (live.size() == 1 || allAlike(gameData, live)) {
                chosen = live.getFirst();
            } else {
                var context = new PermanentChoiceContext.FightPairing(fight, picker, choosingFighter,
                        pickers.subList(i + 1, pickers.size()), live, fighters, opponents);
                gameData.interaction.setPermanentChoiceContext(context);
                Permanent asked = gameQueryService.findPermanentById(gameData, picker);
                playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), live, context,
                        choosingFighter
                                ? "Choose the creature that fights " + FaceDownVisibility.publicName(gameData, asked)
                                + "."
                                : "Choose the creature " + FaceDownVisibility.publicName(gameData, asked)
                                + " fights.");
                return;
            }
            left.remove(chosen);
            fighters.add(choosingFighter ? chosen : picker);
            opponents.add(choosingFighter ? picker : chosen);
        }
        fightSupport.fightTogether(gameData, entry, permanents(gameData, fighters), permanents(gameData, opponents));
    }

    /** Whether choosing among {@code ids} changes nothing: every one is like the first. */
    private boolean allAlike(GameData gameData, List<UUID> ids) {
        Permanent first = gameQueryService.findPermanentById(gameData, ids.getFirst());
        return ids.stream().map(id -> gameQueryService.findPermanentById(gameData, id))
                .allMatch(other -> alike(gameData, first, other));
    }

    private boolean alike(GameData gameData, Permanent one, Permanent other) {
        return one.getCard().isToken() && other.getCard().isToken()
                && Objects.equals(one.getCard().getName(), other.getCard().getName())
                && gameQueryService.getEffectivePower(gameData, one) == gameQueryService.getEffectivePower(gameData, other)
                && gameQueryService.getEffectiveToughness(gameData, one)
                == gameQueryService.getEffectiveToughness(gameData, other)
                && one.getMarkedDamage() == other.getMarkedDamage()
                && one.getCounters().equals(other.getCounters())
                && Objects.equals(gameQueryService.findPermanentController(gameData, one.getId()),
                gameQueryService.findPermanentController(gameData, other.getId()));
    }

    private List<UUID> creatureIds(GameData gameData, List<Permanent> permanents) {
        return permanents.stream().filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId).distinct().toList();
    }

    private boolean isCreature(GameData gameData, UUID id) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, id);
        return permanent != null && gameQueryService.isCreature(gameData, permanent);
    }

    private List<Permanent> permanents(GameData gameData, List<UUID> ids) {
        return ids.stream().map(id -> gameQueryService.findPermanentById(gameData, id)).toList();
    }
}

package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Runs a land's mana through every applicable land-mana replacement (Deep Water, Infernal
 * Darkness, Pale Moon, Pulse of Llanowar, Damping Sphere, Mana Reflection, …) in the order its
 * player chooses (CR 616.1). Each "produces X instead of any other type" effect still applies after
 * another, so the last one applied decides the type, while amount changes compose in the order
 * applied.
 *
 * <p>When only {@code ManaReflection}-style multipliers apply, the result is the land's ordinary
 * production multiplied, which the tap paths already compute themselves.
 */
@Service
public class LandManaReplacementService {

    private static final String MULTIPLIER_KEY = "ManaProductionMultiplier:";

    private final ReplacementOrderService replacementOrderService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public LandManaReplacementService(ReplacementOrderService replacementOrderService,
                                      @Lazy InteractionHandlerRegistry interactionHandlerRegistry) {
        this.replacementOrderService = replacementOrderService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    /** What a land's tap for mana turns into once its replacement effects are applied. */
    public sealed interface Resolution {

        /** No replacement changes the type; the tap path produces its usual mana. */
        record Unchanged() implements Resolution {}

        /** The player is choosing which replacement to apply next; no mana exists yet. */
        record Prompted() implements Resolution {}

        /**
         * The mana was produced, or — when several colors remain possible — the player is being
         * asked for the color through the usual mana-color choice.
         */
        record Produced(LandManaEvent event) implements Resolution {}
    }

    /**
     * Applies the land-mana replacements that modify {@code land}'s production of
     * {@code baseAmount} mana of {@code baseColors} for {@code playerId}, then adds the result to
     * that player's pool.
     *
     * @param production the plain mana-color choice describing the source's mana tags
     */
    public Resolution produce(GameData gameData, UUID playerId, Permanent land, Set<ManaColor> baseColors,
                              int baseAmount, ChoiceContext.ManaColorChoice production) {
        if (baseAmount <= 0) {
            return new Resolution.Unchanged();
        }
        LandManaEvent event = new LandManaEvent(playerId, land.getId(), baseColors, baseAmount, production, Set.of());
        return switch (replacementOrderService.run(gameData, event)) {
            case ReplacementOrderService.Outcome.Prompted<LandManaEvent> ignored -> new Resolution.Prompted();
            case ReplacementOrderService.Outcome.Suspended<LandManaEvent> ignored ->
                    throw new IllegalStateException("Land-mana replacements never wait for another decision");
            case ReplacementOrderService.Outcome.Completed<LandManaEvent> completed -> {
                LandManaEvent result = completed.event();
                if (result.appliedKeys().stream().allMatch(key -> key.startsWith(MULTIPLIER_KEY))) {
                    yield new Resolution.Unchanged();
                }
                addOrAskForColor(gameData, result);
                yield new Resolution.Produced(result);
            }
        };
    }

    /**
     * Every color and the largest amount {@code land} could produce for {@code playerId} under the
     * active replacements, or {@code null} when no replacement changes its type. For previews.
     */
    public LandManaEvent possibleProduction(GameData gameData, UUID playerId, Permanent land,
                                            Set<ManaColor> baseColors, int baseAmount) {
        LandManaEvent event = new LandManaEvent(playerId, land.getId(), baseColors, Math.max(1, baseAmount),
                new ChoiceContext.ManaColorChoice(playerId, false, 0), Set.of());
        Set<LandManaEvent> outcomes = replacementOrderService.possibleOutcomes(gameData, event);
        if (outcomes.size() == 1 && outcomes.iterator().next().equals(event.withoutAppliedKeys())) {
            return null;
        }
        Set<ManaColor> colors = new HashSet<>();
        int amount = 0;
        for (LandManaEvent outcome : outcomes) {
            colors.addAll(outcome.colors());
            amount = Math.max(amount, outcome.amount());
        }
        return event.withColors(colors).withAmount(amount);
    }

    /**
     * Finishes an event whose last replacement was chosen through a prompt. Returns the single
     * color to add through the mana-color choice handler, or {@code null} after asking the player
     * to choose among several colors.
     */
    public ManaColor finishAfterChoice(GameData gameData, LandManaEvent event) {
        if (event.colors().size() == 1) {
            return event.colors().iterator().next();
        }
        askForColor(gameData, event);
        return null;
    }

    private void addOrAskForColor(GameData gameData, LandManaEvent event) {
        if (event.colors().size() != 1) {
            askForColor(gameData, event);
            return;
        }
        ChoiceContext.ManaColorChoice production = event.production();
        ManaColor color = ManaProductionSupport.effectiveColor(gameData, event.playerId(),
                event.colors().iterator().next());
        int amount = event.amount();
        ManaPool pool = gameData.playerManaPools.get(event.playerId());
        if (production.fromSnowSource()) {
            pool.addSnowMana(color, amount);
        } else {
            pool.add(color, amount);
        }
        if (production.fromCaveSource()) {
            pool.addCaveManaTag(color, amount);
        }
        if (production.fromDesertSource()) {
            pool.addDesertManaTag(color, amount);
        }
        if (production.fromBasicLandSource()) {
            pool.addBasicLandManaTag(color, amount);
        }
        if (production.fromCreature()) {
            pool.addCreatureMana(color, amount);
        }
    }

    private void askForColor(GameData gameData, LandManaEvent event) {
        List<String> colors = event.colors().stream().sorted().map(Enum::name).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                event.playerId(), null, null, event.production().withAmount(event.amount()), colors,
                "Choose a color of mana to add."));
    }
}

package com.github.laxika.magicalvibes.service.effect.replacement.mana;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.LandManaEvent;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Runs a land's mana through every applicable land-mana replacement (Deep Water, Infernal
 * Darkness, Pale Moon, Pulse of Llanowar, Damping Sphere, Mana Reflection, False Dawn, …) in the
 * order its player chooses (CR 616.1). The result is final: a color chosen afterwards is added
 * as is, without applying "colored mana becomes white" again. Each "produces X instead of any other type" effect still applies after
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
        return produce(gameData, playerId, land, baseColors, baseAmount, production, false);
    }

    /**
     * {@link #produce(GameData, UUID, Permanent, Set, int, ChoiceContext.ManaColorChoice)}; with
     * {@code spellOnly} the mana can be spent only to cast spells (a land tapped through Piracy).
     */
    public Resolution produce(GameData gameData, UUID playerId, Permanent land, Set<ManaColor> baseColors,
                              int baseAmount, ChoiceContext.ManaColorChoice production, boolean spellOnly) {
        if (baseAmount <= 0) {
            return new Resolution.Unchanged();
        }
        LandManaEvent event = new LandManaEvent(playerId, land.getId(), baseColors, baseAmount, production, Set.of(),
                spellOnly);
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
     * What tapping {@code land} for {@code baseAmount} mana of {@code baseColors} could produce for
     * {@code playerId} under the active replacements, computed without changing the game.
     *
     * @param colors      every type the mana could end up as, over every order the player may choose
     * @param amount      the largest amount any order produces
     * @param opensChoice whether the tap would stop for a decision: the order of the replacements
     *                    or the color of the mana
     */
    public record Preview(Set<ManaColor> colors, int amount, boolean opensChoice) {}

    /**
     * The {@link Preview} of {@code land}'s mana, or {@code null} when no replacement changes its
     * type, so the land's own mana abilities describe it. For previews and AI planning.
     */
    public Preview preview(GameData gameData, UUID playerId, Permanent land, Set<ManaColor> baseColors,
                           int baseAmount) {
        LandManaEvent event = new LandManaEvent(playerId, land.getId(), baseColors, Math.max(1, baseAmount),
                new ChoiceContext.ManaColorChoice(playerId, false, 0), Set.of(), false);
        Set<LandManaEvent> outcomes = replacementOrderService.possibleOutcomes(gameData, event);
        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        int amount = 0;
        boolean severalColors = false;
        for (LandManaEvent outcome : outcomes) {
            colors.addAll(outcome.colors());
            amount = Math.max(amount, outcome.amount());
            severalColors |= outcome.colors().size() > 1;
        }
        if (colors.equals(Set.copyOf(baseColors))) {
            return null;
        }
        return new Preview(colors, amount, outcomes.size() > 1 || severalColors);
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
        ManaColor color = event.colors().iterator().next();
        int amount = event.amount();
        ManaPool pool = gameData.playerManaPools.get(event.playerId());
        if (production.creatureSpellOnly()) {
            if (production.fromCreature()) {
                pool.addCreatureSourceCreatureSpellOnlyMana(color, amount);
            } else {
                pool.addCreatureSpellOnlyMana(color, amount);
            }
        } else if (production.fromSnowSource()) {
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
        if (production.fromCreature() && !production.creatureSpellOnly()) {
            pool.addCreatureMana(color, amount);
        }
        if (event.spellOnly()) {
            pool.addSpellOnlyMana(color, amount);
        }
    }

    private void askForColor(GameData gameData, LandManaEvent event) {
        List<String> colors = event.colors().stream().sorted().map(Enum::name).toList();
        ChoiceContext context = event.spellOnly()
                ? new ChoiceContext.SpellOnlyManaColorChoice(event.playerId(), event.production().fromCreature(),
                        event.amount(), false).withReplacementsApplied()
                : event.production().withAmount(event.amount()).withReplacementsApplied();
        PendingInteraction.ColorChoice choice = new PendingInteraction.ColorChoice(
                event.playerId(), null, null, context, colors,
                event.spellOnly() ? "Choose a color of mana to add (spells only)." : "Choose a color of mana to add.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.queueInteraction(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }
}

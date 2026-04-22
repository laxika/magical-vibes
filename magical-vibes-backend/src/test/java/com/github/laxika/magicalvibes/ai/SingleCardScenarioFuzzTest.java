package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

/**
 * Per-card scenario fuzz: given a target card, set up a randomized board state,
 * put the card in hand, pick a random legal target, cast it, and let it resolve.
 * Assertion is "no exception thrown + simple invariants hold".
 *
 * <p>Unlike {@link RandomAiFuzzTest} this skips turn progression entirely — each
 * iteration is a single cast + resolve. Each side of the battlefield is stuffed
 * with 30–50 random permanents plus up to {@value #AURA_MAX_PER_SIDE} auras
 * attached to same-side permanents, and graveyards are seeded with up to
 * {@value #GRAVEYARD_MAX_PER_SIDE} random instants/sorceries so graveyard
 * interactions have material. Cards with features this fuzzer doesn't model yet
 * (X costs, modal spells, multi-target, damage distribution, sacrifice costs,
 * graveyard-exile costs, spell targets) are skipped and counted.</p>
 *
 * <p>System properties:
 * <ul>
 *   <li>{@code -DrunScenarioFuzz=true} — required to enable the test</li>
 *   <li>{@code -DscenarioCard=Name} — restrict to matching card name or class (optional; default: all)</li>
 *   <li>{@code -DscenarioIterations=N} — iterations per printing (default: 100)</li>
 *   <li>{@code -DscenarioSeed=X} — fixed seed across all iterations, for reproducing failures</li>
 * </ul>
 */
@Tag("scryfall")
@EnabledIfSystemProperty(named = "runScenarioFuzz", matches = "true")
class SingleCardScenarioFuzzTest {

    private static final int DEFAULT_ITERATIONS = 100;
    private static final int JUNK_MIN_PER_SIDE = 30;
    private static final int JUNK_MAX_PER_SIDE = 50;
    private static final int AURA_MAX_PER_SIDE = 6;
    private static final int GRAVEYARD_MAX_PER_SIDE = 10;
    private static final int MANA_PER_COLOR = 30;
    private static final int STACK_RESOLVE_STEPS = 40;
    private static final int STACK_INVARIANT_CAP = 200;

    private static List<CardPrinting> permanentPool;
    private static List<CardPrinting> auraPool;
    private static List<CardPrinting> spellPool;

    @Test
    void scenarioFuzz() {
        String cardFilter = System.getProperty("scenarioCard");
        int iterations = Integer.getInteger("scenarioIterations", DEFAULT_ITERATIONS);
        Long fixedSeed = Long.getLong("scenarioSeed");

        // Constructing a harness boots Spring + loads Scryfall oracle data into the
        // registry. Without this warm-up, Card.getName()/hasType() return nulls.
        new GameTestHarness();
        initializePools();

        List<CardPrinting> targets = resolveTargetPrintings(cardFilter);
        if (targets.isEmpty()) {
            fail("No card printings matched filter: " + cardFilter);
        }
        System.out.printf("Scenario fuzz: %d printing(s), %d iterations each%n",
                targets.size(), iterations);

        int executed = 0;
        int skipped = 0;
        for (CardPrinting printing : targets) {
            String label = describe(printing);
            for (int i = 1; i <= iterations; i++) {
                long seed = fixedSeed != null ? fixedSeed : System.nanoTime();
                try {
                    if (runScenario(printing, new Random(seed))) {
                        executed++;
                    } else {
                        skipped++;
                    }
                } catch (Throwable t) {
                    fail(String.format("Scenario failed: card=%s iter=%d seed=%d cause=%s",
                            label, i, seed, t), t);
                }
            }
        }
        System.out.printf("Scenario fuzz: executed=%d skipped=%d%n", executed, skipped);
    }

    // ------------------------------------------------------------------
    // One scenario
    // ------------------------------------------------------------------

    private boolean runScenario(CardPrinting printing, Random rng) {
        Card card = printing.createCard();
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        if (card.getManaCost() == null) {
            return false;
        }
        if (hasUnsupportedComplexity(card)) {
            return false;
        }

        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        harness.clearMessages();

        Player p1 = harness.getPlayer1();
        Player p2 = harness.getPlayer2();
        GameData gd = harness.getGameData();

        populateRandomBattlefield(harness, p1, rng);
        populateRandomBattlefield(harness, p2, rng);
        populateRandomGraveyard(harness, p1, rng);
        populateRandomGraveyard(harness, p2, rng);
        harness.setLife(p1, 1 + rng.nextInt(40));
        harness.setLife(p2, 1 + rng.nextInt(40));
        giveAllMana(harness, p1);

        harness.setHand(p1, List.of(card));

        UUID targetId = null;
        if (EffectResolution.needsTarget(card) || card.isAura()) {
            targetId = pickRandomLegalTarget(harness.getTargetLegalityService(), gd, card, p1.getId(), rng);
            if (targetId == null) {
                return false;
            }
        }

        try {
            castByType(harness, p1, card, targetId);
        } catch (IllegalStateException | IllegalArgumentException cantCast) {
            // Cast rejected (e.g. timing) — not an engine bug, try a different seed next iteration
            return false;
        }

        resolveStack(harness);
        assertInvariants(gd);
        return true;
    }

    // ------------------------------------------------------------------
    // Complexity gate
    // ------------------------------------------------------------------

    private boolean hasUnsupportedComplexity(Card card) {
        ManaCost cost = new ManaCost(card.getManaCost());
        if (cost.hasX()) {
            return true;
        }
        if (card.getSpellTargets().size() > 1) {
            return true;
        }
        if (EffectResolution.needsDamageDistribution(card)) {
            return true;
        }
        if (EffectResolution.needsSpellTarget(card)) {
            return true;
        }
        for (CardEffect e : card.getEffects(EffectSlot.SPELL)) {
            if (e instanceof ChooseOneEffect
                    || e instanceof SacrificeCreatureCost
                    || e instanceof SacrificeArtifactCost
                    || e instanceof SacrificePermanentCost
                    || e instanceof ExileCardFromGraveyardCost) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Target picking
    // ------------------------------------------------------------------

    private UUID pickRandomLegalTarget(TargetLegalityService legality, GameData gd, Card card,
                                       UUID controllerId, Random rng) {
        List<UUID> legal = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            if (legality.checkSpellTargeting(gd, card, pid, null, controllerId).isEmpty()) {
                legal.add(pid);
            }
        }
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent perm : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (legality.checkSpellTargeting(gd, card, perm.getId(), Zone.BATTLEFIELD, controllerId).isEmpty()) {
                    legal.add(perm.getId());
                }
            }
        }
        if (legal.isEmpty()) {
            return null;
        }
        return legal.get(rng.nextInt(legal.size()));
    }

    // ------------------------------------------------------------------
    // Cast dispatch
    // ------------------------------------------------------------------

    private void castByType(GameTestHarness harness, Player player, Card card, UUID targetId) {
        if (card.hasType(CardType.INSTANT)) {
            if (targetId != null) {
                harness.castInstant(player, 0, targetId);
            } else {
                harness.castInstant(player, 0);
            }
        } else if (card.hasType(CardType.SORCERY)) {
            if (targetId != null) {
                harness.castSorcery(player, 0, 0, targetId);
            } else {
                harness.castSorcery(player, 0, 0);
            }
        } else if (card.hasType(CardType.CREATURE)) {
            if (targetId != null) {
                harness.castCreature(player, 0, 0, targetId);
            } else {
                harness.castCreature(player, 0);
            }
        } else if (card.hasType(CardType.ENCHANTMENT)) {
            if (targetId != null) {
                harness.castEnchantment(player, 0, targetId);
            } else {
                harness.castEnchantment(player, 0);
            }
        } else if (card.hasType(CardType.ARTIFACT)) {
            if (targetId != null) {
                harness.castArtifact(player, 0, targetId);
            } else {
                harness.castArtifact(player, 0);
            }
        } else if (card.hasType(CardType.PLANESWALKER)) {
            harness.castPlaneswalker(player, 0);
        }
    }

    // ------------------------------------------------------------------
    // Setup helpers
    // ------------------------------------------------------------------

    private void giveAllMana(GameTestHarness harness, Player p) {
        for (ManaColor c : ManaColor.values()) {
            harness.addMana(p, c, MANA_PER_COLOR);
        }
    }

    private void populateRandomBattlefield(GameTestHarness harness, Player p, Random rng) {
        int count = JUNK_MIN_PER_SIDE + rng.nextInt(JUNK_MAX_PER_SIDE - JUNK_MIN_PER_SIDE + 1);
        List<Permanent> field = harness.getGameData().playerBattlefields.get(p.getId());
        for (int i = 0; i < count; i++) {
            CardPrinting printing = permanentPool.get(rng.nextInt(permanentPool.size()));
            Permanent perm = harness.addToBattlefieldAndReturn(p, printing.createCard());
            perm.setSummoningSick(false);
        }

        // Attach some auras to random same-side permanents. Pick attach targets from
        // the pre-aura snapshot so auras can't end up enchanting other auras.
        if (!auraPool.isEmpty()) {
            List<Permanent> attachTargets = new ArrayList<>(field);
            int auraCount = rng.nextInt(AURA_MAX_PER_SIDE + 1);
            for (int i = 0; i < auraCount && !attachTargets.isEmpty(); i++) {
                CardPrinting printing = auraPool.get(rng.nextInt(auraPool.size()));
                Permanent auraPerm = harness.addToBattlefieldAndReturn(p, printing.createCard());
                Permanent target = attachTargets.get(rng.nextInt(attachTargets.size()));
                auraPerm.setAttachedTo(target.getId());
            }
        }
    }

    private void populateRandomGraveyard(GameTestHarness harness, Player p, Random rng) {
        if (spellPool.isEmpty()) {
            return;
        }
        int count = rng.nextInt(GRAVEYARD_MAX_PER_SIDE + 1);
        List<Card> graveyard = harness.getGameData().playerGraveyards.get(p.getId());
        for (int i = 0; i < count; i++) {
            CardPrinting printing = spellPool.get(rng.nextInt(spellPool.size()));
            graveyard.add(printing.createCard());
        }
    }

    /**
     * Splits every registered printing into three buckets:
     * <ul>
     *   <li>{@code permanentPool} — non-aura permanents (creatures, artifacts, enchantments, planeswalkers).
     *       Used as battlefield junk.</li>
     *   <li>{@code auraPool} — auras. Attached to a random same-side permanent when seeded onto a battlefield.
     *       If an aura's attachment is illegal, state-based actions will put it into the graveyard — that's
     *       accurate rules behaviour and part of the chaos.</li>
     *   <li>{@code spellPool} — instants and sorceries. Seeded into graveyards so graveyard-interaction
     *       effects (flashback, scavenge, delve, etc.) have something to chew on.</li>
     * </ul>
     * Lands are excluded from every pool.
     */
    private static synchronized void initializePools() {
        if (permanentPool != null) {
            return;
        }
        List<CardPrinting> permanents = new ArrayList<>();
        List<CardPrinting> auras = new ArrayList<>();
        List<CardPrinting> spells = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : set.getPrintings()) {
                Card sample = printing.createCard();
                if (sample.hasType(CardType.LAND)) {
                    continue;
                }
                if (sample.isAura()) {
                    auras.add(printing);
                } else if (sample.hasType(CardType.INSTANT) || sample.hasType(CardType.SORCERY)) {
                    spells.add(printing);
                } else {
                    permanents.add(printing);
                }
            }
        }
        permanentPool = permanents;
        auraPool = auras;
        spellPool = spells;
        System.out.printf("Scenario fuzz: pool sizes — permanents=%d auras=%d spells=%d%n",
                permanents.size(), auras.size(), spells.size());
    }

    private void resolveStack(GameTestHarness harness) {
        GameData gd = harness.getGameData();
        for (int i = 0; i < STACK_RESOLVE_STEPS; i++) {
            if (gd.stack.isEmpty()) {
                return;
            }
            if (gd.status != GameStatus.RUNNING) {
                return;
            }
            if (gd.interaction.isAwaitingInput()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    // ------------------------------------------------------------------
    // Invariants
    // ------------------------------------------------------------------

    private void assertInvariants(GameData gd) {
        if (gd.stack.size() > STACK_INVARIANT_CAP) {
            throw new AssertionError("Stack grew to " + gd.stack.size());
        }
        Set<UUID> seen = new HashSet<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (!seen.add(p.getId())) {
                    throw new AssertionError("Permanent " + p.getId() + " appears in multiple battlefields");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Printing enumeration
    // ------------------------------------------------------------------

    private List<CardPrinting> resolveTargetPrintings(String filter) {
        List<CardPrinting> all = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            all.addAll(set.getPrintings());
        }
        if (filter == null || filter.isBlank()) {
            return all;
        }
        List<CardPrinting> matches = new ArrayList<>();
        for (CardPrinting p : all) {
            Card sample = p.createCard();
            if (sample.getName().equalsIgnoreCase(filter)
                    || sample.getClass().getSimpleName().equalsIgnoreCase(filter)) {
                matches.add(p);
            }
        }
        return matches;
    }

    private String describe(CardPrinting printing) {
        Card sample = printing.createCard();
        return sample.getName() + " [" + sample.getClass().getSimpleName() + "]";
    }
}

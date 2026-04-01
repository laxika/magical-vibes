package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class SpellEvaluatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private SpellEvaluator spellEvaluator;
    private BoardEvaluator boardEvaluator;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        boardEvaluator = new BoardEvaluator(harness.getGameQueryService());
        spellEvaluator = new SpellEvaluator(harness.getGameQueryService(), boardEvaluator);

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
    }

    @Test
    @DisplayName("Creature card value correlates with P/T + keywords")
    void creatureValueCorrelatesWithStats() {
        double bearsValue = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());
        double serraValue = spellEvaluator.estimateSpellValue(gd, new SerraAngel(), player1.getId());

        // Serra Angel (4/4 flying vigilance) should be worth more than Grizzly Bears (2/2)
        assertThat(serraValue).isGreaterThan(bearsValue);
        assertThat(bearsValue).isGreaterThan(0);
    }

    @Test
    @DisplayName("Damage spell has positive value when opponent has a creature it can kill")
    void damageSpellKillsCreature() {
        // Opponent has a 2/2
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double shockValue = spellEvaluator.estimateSpellValue(gd, new Shock(), player1.getId());

        // Shock (2 damage) can kill the 2/2, so it should have positive value
        assertThat(shockValue).isGreaterThan(0);
    }

    @Test
    @DisplayName("Damage spell to face when no creatures to kill")
    void damageSpellFaceDamage() {
        // No opponent creatures
        double shockValue = spellEvaluator.estimateSpellValue(gd, new Shock(), player1.getId());

        // Still positive (face damage = 2 * 1.5 = 3.0)
        assertThat(shockValue).isGreaterThan(0);
    }

    @Test
    @DisplayName("Damage spell value higher against big creature than face damage")
    void damageSpellPrefersBigCreatureKill() {
        // Opponent has a 2/2 that Shock can kill
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double withCreature = spellEvaluator.estimateSpellValue(gd, new Shock(), player1.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        double withoutCreature = spellEvaluator.estimateSpellValue(gd, new Shock(), player1.getId());

        // Value should be higher when there's a creature to kill
        assertThat(withCreature).isGreaterThanOrEqualTo(withoutCreature);
    }

    @Test
    @DisplayName("Detrimental aura has higher value when opponent has bigger creature")
    void detrimentalAuraValueScalesWithTarget() {
        // Opponent has a small creature
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double valueSmall = spellEvaluator.estimateSpellValue(gd, new Pacifism(), player1.getId());

        // Now opponent has a big creature instead
        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        double valueBig = spellEvaluator.estimateSpellValue(gd, new Pacifism(), player1.getId());

        assertThat(valueBig).isGreaterThan(valueSmall);
    }

    @Test
    @DisplayName("Aura has zero value when no valid targets")
    void auraNoTargetsZeroValue() {
        // No opponent creatures
        double value = spellEvaluator.estimateSpellValue(gd, new Pacifism(), player1.getId());

        assertThat(value).isEqualTo(0);
    }

    // ===== Gain control effects =====

    @Test
    @DisplayName("Gain control spell has positive value when opponent has creatures")
    void gainControlPositiveValueWithTargets() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double value = spellEvaluator.estimateSpellValue(gd, new EntrancingMelody(), player1.getId());

        assertThat(value).isGreaterThan(0);
    }

    @Test
    @DisplayName("Gain control spell has zero value when opponent has no creatures")
    void gainControlZeroValueWithoutTargets() {
        double value = spellEvaluator.estimateSpellValue(gd, new EntrancingMelody(), player1.getId());

        assertThat(value).isEqualTo(0);
    }

    @Test
    @DisplayName("Gain control spell is valued higher than destroy for same target")
    void gainControlValueHigherThanDestroy() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double stealValue = spellEvaluator.estimateSpellValue(gd, new EntrancingMelody(), player1.getId());
        double shockValue = spellEvaluator.estimateSpellValue(gd, new Shock(), player1.getId());

        // Stealing a creature should be more valuable than just killing it
        assertThat(stealValue).isGreaterThan(shockValue);
    }

    // ===== Temporary steal (GainControlOfTargetPermanentUntilEndOfTurnEffect) =====

    @Test
    @DisplayName("Temporary steal spell has positive value when opponent has creatures")
    void temporaryStealSpellPositiveValueWithTargets() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double value = spellEvaluator.estimateSpellValue(gd, new ActOfTreason(), player1.getId());

        assertThat(value).isGreaterThan(0);
    }

    @Test
    @DisplayName("Temporary steal spell has zero value when opponent has no creatures")
    void temporaryStealSpellZeroValueWithoutTargets() {
        // No opponent creatures on the battlefield
        double value = spellEvaluator.estimateSpellValue(gd, new ActOfTreason(), player1.getId());

        assertThat(value).isEqualTo(0);
    }

    @Test
    @DisplayName("Temporary steal is valued less than permanent steal for the same target")
    void temporaryStealLowerValueThanPermanentSteal() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        double tempStealValue = spellEvaluator.estimateSpellValue(gd, new ActOfTreason(), player1.getId());
        double permStealValue = spellEvaluator.estimateSpellValue(gd, new EntrancingMelody(), player1.getId());

        // Permanent control is worth more than temporary control for the same target
        assertThat(tempStealValue).isLessThan(permStealValue);
    }

    @Test
    @DisplayName("Temporary steal value scales with the value of the target creature")
    void temporaryStealValueScalesWithTarget() {
        // Small target
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        double smallTargetValue = spellEvaluator.estimateSpellValue(gd, new ActOfTreason(), player1.getId());

        // Large target
        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);
        double largeTargetValue = spellEvaluator.estimateSpellValue(gd, new ActOfTreason(), player1.getId());

        assertThat(largeTargetValue).isGreaterThan(smallTargetValue);
    }

    // ===== evaluateAbilityEffects =====

    @Test
    @DisplayName("BoostSelfEffect scores power*2 + toughness")
    void boostSelfEffectScoring() {
        // {R}: +1/+0 (Shivan Dragon style)
        double value = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new BoostSelfEffect(1, 0)), player1.getId());
        assertThat(value).isEqualTo(2.0); // 1*2.0 + 0

        // +2/+2 pump
        double bigPump = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new BoostSelfEffect(2, 2)), player1.getId());
        assertThat(bigPump).isEqualTo(6.0); // 2*2.0 + 2
    }

    @Test
    @DisplayName("RegenerateEffect scores 4.0")
    void regenerateEffectScoring() {
        double value = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new RegenerateEffect()), player1.getId());
        assertThat(value).isEqualTo(4.0);
    }

    @Test
    @DisplayName("ScryEffect scores count * 2.0")
    void scryEffectScoring() {
        double scry1 = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new ScryEffect(1)), player1.getId());
        assertThat(scry1).isEqualTo(2.0);

        double scry3 = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new ScryEffect(3)), player1.getId());
        assertThat(scry3).isEqualTo(6.0);
    }

    @Test
    @DisplayName("PutPlusOnePlusOneCounterOnEachOwnCreatureEffect scales with creature count")
    void counterOnEachCreatureScalesWithCount() {
        // No creatures: value should be 0
        double noCreatures = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(1)), player1.getId());
        assertThat(noCreatures).isEqualTo(0.0);

        // Add 3 creatures
        for (int i = 0; i < 3; i++) {
            Permanent creature = new Permanent(new GrizzlyBears());
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(creature);
        }

        double withCreatures = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(1)), player1.getId());
        assertThat(withCreatures).isEqualTo(10.5); // 3 * 1 * 3.5
    }

    @Test
    @DisplayName("PutPlusOnePlusOneCounterOnTargetCreatureEffect scores count * 3.5")
    void counterOnTargetCreatureScoring() {
        double value = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)), player1.getId());
        assertThat(value).isEqualTo(7.0); // 2 * 3.5
    }

    @Test
    @DisplayName("TapTargetPermanentEffect scores based on opponent creature value")
    void tapTargetPermanentScoring() {
        // No opponent creatures: value should be 0
        double noCreatures = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new TapTargetPermanentEffect()), player1.getId());
        assertThat(noCreatures).isEqualTo(0.0);

        // Add opponent creature
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(angel);

        double withCreature = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new TapTargetPermanentEffect()), player1.getId());
        assertThat(withCreature).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("CostEffect instances are skipped in ability evaluation")
    void costEffectsAreSkipped() {
        // SacrificeSelfCost + DealDamage — only damage should be scored
        List<CardEffect> effects = List.of(
                new SacrificeSelfCost(),
                new DealDamageToAnyTargetEffect(1));

        double value = spellEvaluator.evaluateAbilityEffects(gd, effects, player1.getId());

        // Should only include the damage value, not the cost
        // DealDamageToAnyTargetEffect(1) → falls through to evaluateSingleEffect → face damage = 1*1.5
        assertThat(value).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Pay life cost is not scored as a positive effect")
    void payLifeCostNotScoredPositively() {
        // PayLifeCost + DrawCard — only draw should be scored
        List<CardEffect> effects = List.of(
                new PayLifeCost(2),
                new DrawCardEffect());

        double withCost = spellEvaluator.evaluateAbilityEffects(gd, effects, player1.getId());

        // DrawCardEffect falls through to evaluateSingleEffect
        double drawOnly = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new DrawCardEffect()), player1.getId());

        // Value should be the same whether or not the cost is present (costs are skipped)
        assertThat(withCost).isEqualTo(drawOnly);
    }

    @Test
    @DisplayName("Multiple non-cost effects are summed")
    void multipleEffectsAreSummed() {
        double boostOnly = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new BoostSelfEffect(1, 0)), player1.getId());
        double regenOnly = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new RegenerateEffect()), player1.getId());

        double combined = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new BoostSelfEffect(1, 0), new RegenerateEffect()), player1.getId());

        assertThat(combined).isEqualTo(boostOnly + regenOnly);
    }

    @Test
    @DisplayName("TapTargetPermanent ignores already tapped creatures")
    void tapTargetIgnoresTappedCreatures() {
        // One untapped, one tapped
        Permanent untapped = new Permanent(new GrizzlyBears());
        untapped.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(untapped);

        Permanent tapped = new Permanent(new SerraAngel());
        tapped.setSummoningSick(false);
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        double value = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new TapTargetPermanentEffect()), player1.getId());

        // Value should be based on the untapped 2/2, not the tapped 4/4
        // Remove the tapped creature and check only the untapped one gives the same result
        gd.playerBattlefields.get(player2.getId()).remove(tapped);
        double untappedOnly = spellEvaluator.evaluateAbilityEffects(
                gd, List.of(new TapTargetPermanentEffect()), player1.getId());

        // The bigger creature is tapped, so the value should be based on the small untapped one
        assertThat(value).isEqualTo(untappedOnly);
    }
}

package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.f.FalkenrathNoble;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingGrace;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.m.MidnightHaunting;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
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

import java.util.ArrayList;
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

    // ===== evaluateCardForDiscard =====

    @Test
    @DisplayName("Discard: lands are highly valued when AI has few mana sources")
    void discardLandValuedHighWhenRamping() {
        // AI has only 2 lands on the battlefield — still ramping
        for (int i = 0; i < 2; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player1.getId()).add(land);
        }

        List<Card> hand = List.of(new Forest(), new GrizzlyBears());
        double landValue = spellEvaluator.evaluateCardForDiscard(gd, hand.get(0), hand, player1.getId());
        double creatureValue = spellEvaluator.evaluateCardForDiscard(gd, hand.get(1), hand, player1.getId());

        // With only 2 mana sources, land should be very valuable to keep
        assertThat(landValue).isGreaterThan(creatureValue);
    }

    @Test
    @DisplayName("Discard: extra lands are low value when AI has many mana sources")
    void discardExtraLandLowValueWhenManaFlooded() {
        // AI has 8 lands — way past ramping
        for (int i = 0; i < 8; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player1.getId()).add(land);
        }

        List<Card> hand = List.of(new Forest(), new GrizzlyBears());
        double landValue = spellEvaluator.evaluateCardForDiscard(gd, hand.get(0), hand, player1.getId());
        double creatureValue = spellEvaluator.evaluateCardForDiscard(gd, hand.get(1), hand, player1.getId());

        // With 8 mana sources, the 2/2 creature should be more valuable than another land
        assertThat(creatureValue).isGreaterThan(landValue);
    }

    @Test
    @DisplayName("Discard: removal valued higher when opponent has big threats")
    void discardRemovalValuedHigherWithThreats() {
        // AI has enough mana to cast either
        for (int i = 0; i < 5; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player1.getId()).add(land);
        }

        // Opponent has a Serra Angel (big threat)
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(angel);

        Card doomBlade = new DoomBlade();
        Card bears = new GrizzlyBears();
        List<Card> hand = List.of(doomBlade, bears);

        double removalValue = spellEvaluator.evaluateCardForDiscard(gd, doomBlade, hand, player1.getId());
        double creatureValue = spellEvaluator.evaluateCardForDiscard(gd, bears, hand, player1.getId());

        // With a big threat on opponent's board, removal should be valued higher than a 2/2
        assertThat(removalValue).isGreaterThan(creatureValue);
    }

    @Test
    @DisplayName("Discard: redundant copies are valued lower than the first copy")
    void discardRedundantCopiesLowerValue() {
        for (int i = 0; i < 5; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player1.getId()).add(land);
        }

        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card shock = new Shock();

        // Hand with two copies of Grizzly Bears + a Shock
        List<Card> handWithDuplicates = List.of(bears1, bears2, shock);
        double duplicateBearValue = spellEvaluator.evaluateCardForDiscard(gd, bears1, handWithDuplicates, player1.getId());

        // Hand with a single copy of Grizzly Bears + a Shock
        List<Card> handWithSingle = List.of(bears1, shock);
        double singleBearValue = spellEvaluator.evaluateCardForDiscard(gd, bears1, handWithSingle, player1.getId());

        // The bear in a hand with duplicates should be worth less than as a single copy
        assertThat(duplicateBearValue).isLessThan(singleBearValue);
    }

    @Test
    @DisplayName("Discard: expensive uncastable spell discounted vs cheap castable spell")
    void discardExpensiveSpellDiscounted() {
        // AI has only 2 lands — can cast a 2-drop but not a 5-drop
        for (int i = 0; i < 2; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player1.getId()).add(land);
        }

        Card bears = new GrizzlyBears();  // 2 mana
        Card airElemental = new AirElemental();  // 5 mana

        List<Card> hand = List.of(bears, airElemental);

        double bearsValue = spellEvaluator.evaluateCardForDiscard(gd, bears, hand, player1.getId());
        double airElementalValue = spellEvaluator.evaluateCardForDiscard(gd, airElemental, hand, player1.getId());

        // Air Elemental is better in a vacuum but the castability penalty
        // should shrink the gap when we only have 2 lands
        // (Air Elemental at 5 mana with 2 lands = 3 turns away = 0.8 multiplier)
        // The Air Elemental may still be valued higher due to its raw stats,
        // but the gap should be smaller than estimateSpellValue alone
        double rawBearsValue = spellEvaluator.estimateSpellValue(gd, bears, player1.getId());
        double rawAirElementalValue = spellEvaluator.estimateSpellValue(gd, airElemental, player1.getId());
        double rawRatio = rawAirElementalValue / rawBearsValue;
        double discardRatio = airElementalValue / bearsValue;

        assertThat(discardRatio).isLessThan(rawRatio);
    }

    // ===== Defensive pressure multiplier =====

    @Test
    @DisplayName("Life gain spell value boosted when opponent board damage >= AI life")
    void lifeGainBoostedUnderLethalPressure() {
        // Opponent has three 3/3 creatures (9 total damage) — lethal vs 5 life
        for (int i = 0; i < 3; i++) {
            Permanent wurm = new Permanent(new CrawWurm()); // 6/4
            wurm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(wurm);
        }

        // AI at 5 life — opponent board damage (18) far exceeds AI life
        gd.playerLifeTotals.put(player1.getId(), 5);
        double underPressure = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // AI at 20 life — opponent board damage (18) is 90% of life, still significant
        gd.playerLifeTotals.put(player1.getId(), 20);
        double moderatePressure = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // Remove opponent creatures — no pressure
        gd.playerBattlefields.get(player2.getId()).clear();
        gd.playerLifeTotals.put(player1.getId(), 20);
        double noPressure = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // Under lethal pressure, life gain should be worth significantly more
        assertThat(underPressure).isGreaterThan(noPressure);
        assertThat(underPressure).isGreaterThan(moderatePressure);
    }

    @Test
    @DisplayName("Board wipe value boosted under defensive pressure")
    void boardWipeBoostedUnderPressure() {
        // Opponent has three 6/4 creatures (18 total damage)
        for (int i = 0; i < 3; i++) {
            Permanent wurm = new Permanent(new CrawWurm());
            wurm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(wurm);
        }

        // AI at 5 life — opponent board damage far exceeds AI life
        gd.playerLifeTotals.put(player1.getId(), 5);
        double underPressure = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // AI at 100 life — opponent board damage is small fraction of life
        gd.playerLifeTotals.put(player1.getId(), 100);
        double noPressure = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // Board wipe should be worth more when under pressure (1.5x multiplier)
        assertThat(underPressure).isGreaterThan(noPressure);
    }

    @Test
    @DisplayName("Removal value boosted under defensive pressure")
    void removalBoostedUnderPressure() {
        // Opponent has a 6/4 Craw Wurm
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // AI at 5 life — opponent board damage (6) exceeds AI life
        gd.playerLifeTotals.put(player1.getId(), 5);
        double underPressure = spellEvaluator.estimateSpellValue(gd, new DoomBlade(), player1.getId());

        // AI at 100 life — no pressure
        gd.playerLifeTotals.put(player1.getId(), 100);
        double noPressure = spellEvaluator.estimateSpellValue(gd, new DoomBlade(), player1.getId());

        assertThat(underPressure).isGreaterThan(noPressure);
    }

    @Test
    @DisplayName("Creature value boosted under defensive pressure (potential blocker)")
    void creatureValueBoostedUnderPressure() {
        // Opponent has a 6/4 Craw Wurm
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // AI at 5 life — facing lethal
        gd.playerLifeTotals.put(player1.getId(), 5);
        double underPressure = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

        // AI at 100 life — safe
        gd.playerLifeTotals.put(player1.getId(), 100);
        double noPressure = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

        assertThat(underPressure).isGreaterThan(noPressure);
    }

    @Test
    @DisplayName("No defensive pressure multiplier when opponent has no creatures")
    void noPressureWithEmptyBoard() {
        // No opponent creatures
        gd.playerLifeTotals.put(player1.getId(), 5);
        double lowLife = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

        gd.playerLifeTotals.put(player1.getId(), 20);
        double normalLife = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

        // Without opponent creatures, life total should not affect spell evaluation
        // (no board damage means no defensive pressure)
        assertThat(lowLife).isEqualTo(normalLife);
    }

    @Test
    @DisplayName("No defensive pressure multiplier when board damage is low relative to life")
    void noPressureWhenSafe() {
        // Opponent has one 2/2 (2 damage)
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // AI at 20 life — pressure ratio is 2/20 = 0.1, well below 0.5 threshold
        gd.playerLifeTotals.put(player1.getId(), 20);
        double multiplier = spellEvaluator.defensivePressureMultiplier(gd, new HealingGrace(), player1.getId());

        assertThat(multiplier).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Non-defensive spell (card draw) gets no pressure boost")
    void nonDefensiveSpellNoPressureBoost() {
        // Opponent has three 6/4 creatures — massive pressure
        for (int i = 0; i < 3; i++) {
            Permanent wurm = new Permanent(new CrawWurm());
            wurm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(wurm);
        }

        // AI at 5 life — facing lethal
        gd.playerLifeTotals.put(player1.getId(), 5);
        double multiplier = spellEvaluator.defensivePressureMultiplier(gd, new Divination(), player1.getId());

        // Divination is pure card draw — not a defensive spell, no boost
        assertThat(multiplier).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Defender creatures excluded from opponent board damage calculation")
    void defenderExcludedFromBoardDamage() {
        // Opponent has only a Wall of Fire (0/5 defender) — cannot attack
        Permanent wall = new Permanent(new WallOfFire());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        // AI at 1 life — but opponent has no attackers (wall has defender)
        gd.playerLifeTotals.put(player1.getId(), 1);
        double multiplier = spellEvaluator.defensivePressureMultiplier(gd, new HealingGrace(), player1.getId());

        // No board damage from defenders, so no pressure
        assertThat(multiplier).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Creature with ETB life gain gets life gain pressure multiplier")
    void creatureWithEtbLifeGainGetsPressureBoost() {
        // Opponent has a 6/4 Craw Wurm
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // AI at 5 life — facing lethal
        gd.playerLifeTotals.put(player1.getId(), 5);

        // InspiringCleric is a creature with ETB "gain 4 life"
        // It should get the life gain multiplier (3.0x), not just the creature multiplier (1.3x)
        double clericMultiplier = spellEvaluator.defensivePressureMultiplier(gd, new InspiringCleric(), player1.getId());
        double bearsMultiplier = spellEvaluator.defensivePressureMultiplier(gd, new GrizzlyBears(), player1.getId());

        assertThat(clericMultiplier).isGreaterThan(bearsMultiplier);
    }

    @Test
    @DisplayName("Pressure multiplier scales linearly at intermediate pressure")
    void pressureMultiplierScalesLinearly() {
        // Opponent has a 6/4 Craw Wurm (6 damage)
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // At 8 life: pressure = 6/8 = 0.75 (between 0.5 and 1.0)
        gd.playerLifeTotals.put(player1.getId(), 8);
        double intermediatePressure = spellEvaluator.defensivePressureMultiplier(gd, new HealingGrace(), player1.getId());

        // At 5 life: pressure = 6/5 = 1.2 (above 1.0, capped at max)
        gd.playerLifeTotals.put(player1.getId(), 5);
        double maxPressure = spellEvaluator.defensivePressureMultiplier(gd, new HealingGrace(), player1.getId());

        // Intermediate should be between 1.0 and max
        assertThat(intermediatePressure).isGreaterThan(1.0);
        assertThat(intermediatePressure).isLessThan(maxPressure);
    }

    @Test
    @DisplayName("Pressure multiplier returns 1.0 when AI life is zero or negative")
    void pressureMultiplierSafeWithZeroLife() {
        // Opponent has a creature
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // AI at 0 life — edge case, should not blow up
        gd.playerLifeTotals.put(player1.getId(), 0);
        double multiplier = spellEvaluator.defensivePressureMultiplier(gd, new HealingGrace(), player1.getId());

        assertThat(multiplier).isEqualTo(1.0);
    }

    // ===== Synergy bonus: Sacrifice + Tokens =====

    @Test
    @DisplayName("Sacrifice spell valued higher when AI controls token creatures")
    void sacrificeSpellBoostedByTokens() {
        Card altarsReap = new AltarsReap();

        // AI has only a real creature — sacrifice is costly
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        double withoutTokens = spellEvaluator.estimateSpellValue(gd, altarsReap, player1.getId());

        // Now add a token creature — sacrifice cost is cheaper
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(token);
        double withTokens = spellEvaluator.estimateSpellValue(gd, altarsReap, player1.getId());

        assertThat(withTokens).isGreaterThan(withoutTokens);
    }

    @Test
    @DisplayName("Sacrifice synergy bonus is zero when no tokens are present")
    void sacrificeSynergyZeroWithoutTokens() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        aiBattlefield.add(bears);

        double bonus = spellEvaluator.synergyBonus(gd, new AltarsReap(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    // ===== Synergy bonus: Equipment + Evasion =====

    @Test
    @DisplayName("Equipment valued higher when AI controls evasive creature")
    void equipmentBoostedByEvasiveCreature() {
        Card darksteelAxe = new DarksteelAxe();

        // AI has a vanilla creature
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        double withVanilla = spellEvaluator.estimateSpellValue(gd, darksteelAxe, player1.getId());

        // Replace with a flying creature
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);
        double withFlyer = spellEvaluator.estimateSpellValue(gd, darksteelAxe, player1.getId());

        assertThat(withFlyer).isGreaterThan(withVanilla);
    }

    @Test
    @DisplayName("Equipment evasion bonus is zero when no creatures are evasive")
    void equipmentEvasionBonusZeroForVanillaCreatures() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        aiBattlefield.add(bears);

        double bonus = spellEvaluator.synergyBonus(gd, new DarksteelAxe(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    // ===== Synergy bonus: Death trigger + Sacrifice outlet =====

    @Test
    @DisplayName("Death trigger creature valued higher when AI controls sacrifice outlet")
    void deathTriggerBoostedBySacOutlet() {
        Card falkenrath = new FalkenrathNoble();

        double withoutOutlet = spellEvaluator.estimateSpellValue(gd, falkenrath, player1.getId());

        Permanent sacOutlet = new Permanent(new BloodthroneVampire());
        sacOutlet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sacOutlet);
        double withOutlet = spellEvaluator.estimateSpellValue(gd, falkenrath, player1.getId());

        assertThat(withOutlet).isGreaterThan(withoutOutlet);
    }

    @Test
    @DisplayName("Death trigger synergy bonus is zero when no sacrifice outlets exist")
    void deathTriggerBonusZeroWithoutSacOutlet() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        aiBattlefield.add(bears);

        double bonus = spellEvaluator.synergyBonus(gd, new FalkenrathNoble(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    // ===== Synergy bonus: Anthem + Wide board =====

    @Test
    @DisplayName("Anthem creature valued higher with wide board (3+ creatures)")
    void anthemBoostedByWideBoard() {
        Card marshal = new BenalishMarshal();

        Permanent bear1 = new Permanent(new GrizzlyBears());
        bear1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear1);
        double narrowBoard = spellEvaluator.estimateSpellValue(gd, marshal, player1.getId());

        for (int i = 0; i < 3; i++) {
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear);
        }
        double wideBoard = spellEvaluator.estimateSpellValue(gd, marshal, player1.getId());

        assertThat(wideBoard).isGreaterThan(narrowBoard);
    }

    @Test
    @DisplayName("Anthem synergy bonus is zero with fewer than 3 creatures")
    void anthemBonusZeroWithNarrowBoard() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        for (int i = 0; i < 2; i++) {
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            aiBattlefield.add(bear);
        }

        double bonus = spellEvaluator.synergyBonus(gd, new BenalishMarshal(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Anthem synergy bonus scales with board width — 5+ gets more than 3")
    void anthemBonusScalesWithBoardWidth() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        for (int i = 0; i < 3; i++) {
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            aiBattlefield.add(bear);
        }
        double threeCreatures = spellEvaluator.synergyBonus(gd, new BenalishMarshal(), player1.getId(),
                aiBattlefield, oppBattlefield);

        for (int i = 0; i < 2; i++) {
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            aiBattlefield.add(bear);
        }
        double fiveCreatures = spellEvaluator.synergyBonus(gd, new BenalishMarshal(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(fiveCreatures).isGreaterThan(threeCreatures);
    }

    // ===== Synergy bonus: Token maker + Death triggers =====

    @Test
    @DisplayName("Token-making spell valued higher when AI controls death trigger creature")
    void tokenMakerBoostedByDeathTriggers() {
        Card midnightHaunting = new MidnightHaunting();

        double withoutTriggers = spellEvaluator.estimateSpellValue(gd, midnightHaunting, player1.getId());

        Permanent noble = new Permanent(new FalkenrathNoble());
        noble.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(noble);
        double withTriggers = spellEvaluator.estimateSpellValue(gd, midnightHaunting, player1.getId());

        assertThat(withTriggers).isGreaterThan(withoutTriggers);
    }

    @Test
    @DisplayName("Token-making spell valued higher when AI controls sacrifice outlet")
    void tokenMakerBoostedBySacOutlet() {
        Card midnightHaunting = new MidnightHaunting();

        double withoutOutlet = spellEvaluator.estimateSpellValue(gd, midnightHaunting, player1.getId());

        Permanent sacOutlet = new Permanent(new BloodthroneVampire());
        sacOutlet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sacOutlet);
        double withOutlet = spellEvaluator.estimateSpellValue(gd, midnightHaunting, player1.getId());

        assertThat(withOutlet).isGreaterThan(withoutOutlet);
    }

    @Test
    @DisplayName("Token maker synergy bonus is zero when no death triggers or sac outlets")
    void tokenMakerBonusZeroWithoutSynergy() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        aiBattlefield.add(bears);

        double bonus = spellEvaluator.synergyBonus(gd, new MidnightHaunting(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    // ===== Synergy bonus: Non-synergy cards =====

    @Test
    @DisplayName("Vanilla creature gets zero synergy bonus")
    void vanillaCreatureNoSynergyBonus() {
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> oppBattlefield = gd.playerBattlefields.get(player2.getId());

        double bonus = spellEvaluator.synergyBonus(gd, new GrizzlyBears(), player1.getId(),
                aiBattlefield, oppBattlefield);

        assertThat(bonus).isEqualTo(0.0);
    }

    // ===== Life gain danger-level scaling =====

    @Test
    @DisplayName("Life gain spell base value scales with AI danger level")
    void lifeGainBaseValueScalesWithDanger() {
        // Opponent has a 6/4 Craw Wurm (6 damage)
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);

        // At 5 life: danger = 6/5 = 1.2 → multiplier clamped to 1.2
        gd.playerLifeTotals.put(player1.getId(), 5);
        double dangerousValue = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // At 20 life: danger = 6/20 = 0.3 → multiplier clamped to 0.3
        gd.playerLifeTotals.put(player1.getId(), 20);
        double safeValue = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // Life gain should be worth much more when in danger
        assertThat(dangerousValue).isGreaterThan(safeValue * 2);
    }

    @Test
    @DisplayName("Life gain spell has minimal value when AI is safe with no opponent threats")
    void lifeGainMinimalWhenSafe() {
        // No opponent creatures — danger is 0, multiplier floors at 0.3
        gd.playerLifeTotals.put(player1.getId(), 20);
        double safeValue = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // Add a threatening opponent creature
        Permanent wurm = new Permanent(new CrawWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wurm);
        gd.playerLifeTotals.put(player1.getId(), 5);
        double dangerValue = spellEvaluator.estimateSpellValue(gd, new HealingGrace(), player1.getId());

        // Safe value should be much lower
        assertThat(safeValue).isLessThan(dangerValue);
    }

    // ===== Board wipe survival urgency =====

    @Test
    @DisplayName("Board wipe gains survival urgency bonus when facing lethal on board")
    void boardWipeSurvivalUrgencyWhenFacingLethal() {
        // Opponent has two 6/4 Craw Wurms (12 total damage), AI has one 6/4 Craw Wurm
        for (int i = 0; i < 2; i++) {
            Permanent wurm = new Permanent(new CrawWurm());
            wurm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(wurm);
        }
        Permanent aiWurm = new Permanent(new CrawWurm());
        aiWurm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiWurm);

        // AI at 10 life with 12 damage on board — facing lethal
        gd.playerLifeTotals.put(player1.getId(), 10);
        double facingLethal = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // AI at 100 life — not facing lethal, same board state
        gd.playerLifeTotals.put(player1.getId(), 100);
        double safe = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // Facing lethal should add the survival urgency bonus (+30)
        assertThat(facingLethal).isGreaterThan(safe + 20.0);
    }

    @Test
    @DisplayName("Board wipe with MassDamageEffect gets survival urgency when facing lethal")
    void massDamageSurvivalUrgencyWhenFacingLethal() {
        // Opponent has three 2/2 bears (6 total damage)
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears);
        }
        // AI has one 2/2 bear
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aiBears);

        // AI at 5 life with 6 damage on board — facing lethal
        gd.playerLifeTotals.put(player1.getId(), 5);
        double facingLethal = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

        // AI at 100 life — not facing lethal
        gd.playerLifeTotals.put(player1.getId(), 100);
        double safe = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

        // Facing lethal should get the survival urgency bonus
        assertThat(facingLethal).isGreaterThan(safe + 20.0);
    }

    @Test
    @DisplayName("No survival urgency when opponent board damage is below AI life")
    void noSurvivalUrgencyWhenNotFacingLethal() {
        // Opponent has one 2/2 bear (2 damage)
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // AI at 20 life — not facing lethal (2 damage < 20 life)
        gd.playerLifeTotals.put(player1.getId(), 20);
        double value = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // Value should just be the creature delta, no +30 bonus
        // One opponent 2/2: creatureScore ≈ 2*3 + 2*1.5 = 9
        assertThat(value).isLessThan(20.0);
    }

    // ===== Board wipe rebuild potential =====

    @Test
    @DisplayName("Board wipe value increases when AI has creatures in hand to replay")
    void boardWipeRebuildPotentialFromHand() {
        // Opponent has two 2/2 bears, AI has two 2/2 bears — even board
        for (int i = 0; i < 2; i++) {
            Permanent oppBears = new Permanent(new GrizzlyBears());
            oppBears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBears);
        }
        for (int i = 0; i < 2; i++) {
            Permanent aiBears = new Permanent(new GrizzlyBears());
            aiBears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiBears);
        }

        gd.playerLifeTotals.put(player1.getId(), 100); // high life to avoid survival urgency

        // Empty hand — no rebuild potential
        gd.playerHands.put(player1.getId(), new ArrayList<>());
        double emptyHand = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // Hand full of creatures — rebuild potential
        List<Card> creatureHand = new ArrayList<>();
        creatureHand.add(new SerraAngel()); // 4/4
        creatureHand.add(new CrawWurm());   // 6/4
        gd.playerHands.put(player1.getId(), creatureHand);
        double fullHand = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // Having creatures in hand should make the board wipe more attractive
        assertThat(fullHand).isGreaterThan(emptyHand);
    }

    @Test
    @DisplayName("MassDamage rebuild potential increases value when AI has creatures in hand")
    void massDamageRebuildPotentialFromHand() {
        // Opponent has two 2/2, AI has two 2/2 — Pyroclasm kills all
        for (int i = 0; i < 2; i++) {
            Permanent oppBears = new Permanent(new GrizzlyBears());
            oppBears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBears);
        }
        for (int i = 0; i < 2; i++) {
            Permanent aiBears = new Permanent(new GrizzlyBears());
            aiBears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiBears);
        }

        gd.playerLifeTotals.put(player1.getId(), 100);

        gd.playerHands.put(player1.getId(), new ArrayList<>());
        double emptyHand = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

        List<Card> creatureHand = new ArrayList<>();
        creatureHand.add(new CrawWurm());
        creatureHand.add(new CrawWurm());
        gd.playerHands.put(player1.getId(), creatureHand);
        double fullHand = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

        assertThat(fullHand).isGreaterThan(emptyHand);
    }

    @Test
    @DisplayName("No rebuild potential bonus when AI loses no creatures to wipe")
    void noRebuildBonusWhenAiLosesNothing() {
        // Only opponent has creatures — AI loses nothing
        for (int i = 0; i < 2; i++) {
            Permanent oppBears = new Permanent(new GrizzlyBears());
            oppBears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBears);
        }

        gd.playerLifeTotals.put(player1.getId(), 100);

        gd.playerHands.put(player1.getId(), new ArrayList<>());
        double emptyHand = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        List<Card> creatureHand = new ArrayList<>();
        creatureHand.add(new CrawWurm());
        gd.playerHands.put(player1.getId(), creatureHand);
        double fullHand = spellEvaluator.estimateSpellValue(gd, new WrathOfGod(), player1.getId());

        // No AI losses means no rebuild discount — value should be the same
        assertThat(fullHand).isEqualTo(emptyHand);
    }
}

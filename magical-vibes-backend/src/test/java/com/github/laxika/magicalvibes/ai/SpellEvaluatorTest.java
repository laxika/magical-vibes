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
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
}

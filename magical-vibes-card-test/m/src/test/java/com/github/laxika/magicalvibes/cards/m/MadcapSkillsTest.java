package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadcapSkills.class, DiscipleOfTheOldWays.class})
class MadcapSkillsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+0")
    void boostsEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attachSkills(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a single creature")
    void cannotBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        attachSkills(attacker);

        Permanent blocker = addCreatureReady(player2, new DiscipleOfTheOldWays());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by two creatures")
    void canBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);
        attachSkills(attacker);

        Permanent blocker1 = addCreatureReady(player2, new DiscipleOfTheOldWays());
        Permanent blocker2 = addCreatureReady(player2, new DiscipleOfTheOldWays());

        prepareDeclareBlockers();

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int idx1 = gd.playerBattlefields.get(player2.getId()).indexOf(blocker1);
        int idx2 = gd.playerBattlefields.get(player2.getId()).indexOf(blocker2);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(idx1, attackerIdx),
                new BlockerAssignment(idx2, attackerIdx)
        ));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Boost and menace end when the Aura leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent aura = attachSkills(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting the Aura on an opponent's creature grants its effects only to that creature")
    void resolvesOnOpponentCreature() {
        Permanent host = addCreatureReady(player2, new DiscipleOfTheOldWays());
        Permanent other = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new MadcapSkills()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Madcap Skills");
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Aura fails to resolve when its target leaves the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent host = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new MadcapSkills()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, host.getId());

        gd.playerBattlefields.get(player1.getId()).remove(host);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Madcap Skills");
        harness.assertNotOnBattlefield(player1, "Madcap Skills");
    }

    @Test
    @DisplayName("Multiple copies add their power bonuses, and menace persists until the last leaves")
    void multipleCopiesApplyIndependently() {
        Permanent host = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent first = attachSkills(host);
        Permanent second = attachSkills(host);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.MENACE)).isFalse();
    }
    @Test
    @DisplayName("Enchant creature rejects a noncreature Aura as a target")
    void cannotTargetNonCreature() {
        Permanent host = addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent aura = attachSkills(host);
        harness.setHand(player1, List.of(new MadcapSkills()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    private Permanent attachSkills(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MadcapSkills());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}

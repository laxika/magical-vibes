package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.s.StalkingYeti;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldMarshal.class, KjeldoranOutrider.class, StalkingYeti.class})
class FieldMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Field Marshal puts it on the stack")
    void castingPutsOnStack() {
        FieldMarshal marshal = new FieldMarshal();
        harness.castFromHand(player1, marshal, "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isSameAs(marshal);
    }

    @Test
    @DisplayName("Resolving puts Field Marshal onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new FieldMarshal(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Field Marshal");
    }

    @Test
    @DisplayName("Field Marshal enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new FieldMarshal(), "{1}{W}{W}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Field Marshal");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Other Soldier creatures get +1/+1 and first strike")
    void buffsOtherSoldiers() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());
        harness.addToBattlefield(player1, new FieldMarshal());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Field Marshal does not buff itself")
    void doesNotBuffItself() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new FieldMarshal());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, marshal, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new StalkingYeti());
        harness.addToBattlefield(player1, new FieldMarshal());

        assertThat(gqs.getEffectivePower(gd, yeti)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, yeti)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, yeti, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Soldier creatures too")
    void buffsOpponentSoldiers() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());

        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentSoldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two Field Marshals buff each other")
    void twoMarshalsBuffEachOther() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new FieldMarshal());

        List<Permanent> marshals = findPermanents(player1, "Field Marshal");

        assertThat(marshals).hasSize(2);
        for (Permanent marshal : marshals) {
            assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, marshal, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Field Marshals give +2/+2 to other Soldiers")
    void twoMarshalsStackBonuses() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Bonus is removed when Field Marshal leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new FieldMarshal());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(marshal);

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Field Marshal resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);

        harness.castFromHand(player1, new FieldMarshal(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Soldier with static buff uses boosted stats in combat")
    void soldierUsesBoostedStatsInCombat() {
        harness.addToBattlefield(player1, new FieldMarshal());

        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new StalkingYeti());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Kjeldoran Outrider");
        harness.assertInGraveyard(player2, "Stalking Yeti");
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());

        soldier.setPowerModifier(soldier.getPowerModifier() + 7);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(10);

        soldier.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opposing Field Marshals buff each other and stack for both players")
    void opposingMarshalsBuffBothBattlefields() {
        Permanent firstMarshal = harness.addToBattlefieldAndReturn(player1, new FieldMarshal());
        Permanent secondMarshal = harness.addToBattlefieldAndReturn(player2, new FieldMarshal());
        Permanent firstSoldier = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutrider());
        Permanent secondSoldier = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());

        for (Permanent marshal : List.of(firstMarshal, secondMarshal)) {
            assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, marshal, Keyword.FIRST_STRIKE)).isTrue();
        }
        for (Permanent soldier : List.of(firstSoldier, secondSoldier)) {
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("A Soldier resolving after Field Marshal immediately receives the bonus")
    void bonusAppliesToLaterSoldier() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.castFromHand(player1, new KjeldoranOutrider(), "{1}{W}");
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Kjeldoran Outrider");
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();
    }
}

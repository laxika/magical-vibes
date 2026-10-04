package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElspethKnightErrant.class, GrizzlyBears.class, Plains.class, Ornithopter.class,
        GloriousAnthem.class, ObeliskOfBant.class})
class ElspethKnightErrantTest extends BaseCardTest {

    @Test
    @DisplayName("First +1 creates a 1/1 white Soldier token")
    void plusOneCreatesSoldierToken() {
        Permanent elspeth = addReadyElspeth(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Second +1 gives target creature +3/+3 and flying")
    void plusOnePumpsTargetAndGrantsFlying() {
        Permanent elspeth = addReadyElspeth(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Second +1 pump and flying wear off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        addReadyElspeth(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Second +1 cannot target a noncreature permanent")
    void plusOneCannotTargetLand() {
        addReadyElspeth(player1);
        harness.addToBattlefield(player2, new Plains());
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 emblem makes controller's artifacts, creatures, enchantments and lands indestructible")
    void ultimateGrantsIndestructibleToOwnPermanents() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 8);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent ownLand = findPermanent(player1, "Plains");
        Permanent opponentCreature = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownLand, Keyword.INDESTRUCTIBLE)).isTrue();
        // Opponent's permanents are unaffected.
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        // Planeswalkers are not in the emblem's type list.
        assertThat(gqs.hasKeyword(gd, elspeth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("-8 emblem also covers artifacts and enchantments; applies to permanents entering later")
    void ultimateCoversArtifactsEnchantmentsAndLaterPermanents() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 8);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent artifact = findPermanent(player1, "Ornithopter");
        Permanent enchantment = findPermanent(player1, "Glorious Anthem");
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantment, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.addToBattlefield(player1, new Plains());
        Permanent laterLand = findPermanent(player1, "Plains");
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate -8 with only starting loyalty")
    void cannotUltimateWithoutEnoughLoyalty() {
        addReadyElspeth(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Emblem protects a noncreature artifact after Elspeth leaves and the turn ends")
    void emblemPersistsAfterSourceLeavesAndTurnEnds() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 8);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elspeth);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A surviving Elspeth does not gain indestructible from her emblem")
    void emblemDoesNotProtectSurvivingPlaneswalker() {
        Permanent elspeth = addReadyElspeth(player1);
        elspeth.setCounterCount(CounterType.LOYALTY, 9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elspeth);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elspeth, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Pump ability does not affect another creature when its target dies before resolution")
    void pumpDoesNotRetargetWhenTargetDies() {
        Permanent elspeth = addReadyElspeth(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target).contains(other);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private Permanent addReadyElspeth(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ElspethKnightErrant());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}

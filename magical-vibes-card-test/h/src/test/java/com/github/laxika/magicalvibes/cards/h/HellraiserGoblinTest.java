package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellraiserGoblin.class, DiscipleOfTheOldWays.class, TurnToFrog.class})
class HellraiserGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("A creature its controller controls must attack while Hellraiser Goblin is out")
    void ownCreatureMustAttack() {
        harness.addToBattlefieldAndReturn(player1, new HellraiserGoblin()).tap();
        addCreatureReady(player1, new DiscipleOfTheOldWays());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Hellraiser Goblin itself must attack too")
    void selfMustAttack() {
        addCreatureReady(player1, new HellraiserGoblin());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Grants haste to the controller's creatures, including itself")
    void grantsHaste() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        harness.addToBattlefield(player1, new DiscipleOfTheOldWays());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Disciple of the Old Ways"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hellraiser Goblin"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's creatures")
    void noHasteForOpponent() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        harness.addToBattlefield(player2, new DiscipleOfTheOldWays());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Disciple of the Old Ways"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not force an opponent's creature to attack")
    void opponentsCreatureNotForced() {
        harness.addToBattlefield(player1, new HellraiserGoblin());
        Permanent bears = addCreatureReady(player2, new DiscipleOfTheOldWays());

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Without Hellraiser Goblin, a creature is free to stay back")
    void notForcedWithoutHellraiser() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());

        declareAttackers(player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can attack thanks to the granted haste")
    void newlyControlledCreaturesCanAttack() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new HellraiserGoblin());
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        goblin.setSummoningSick(true);
        disciple.setSummoningSick(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));

        assertThat(goblin.isAttacking()).isTrue();
        assertThat(disciple.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not required to attack")
    void tappedCreaturesAreNotForced() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new HellraiserGoblin());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        goblin.tap();
        disciple.tap();

        declareAttackers(player1, List.of());

        assertThat(disciple.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Haste and the attack requirement end when Hellraiser Goblin leaves")
    void effectsEndWhenSourceLeaves() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new HellraiserGoblin());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerGraveyards.get(player1.getId()).add(goblin.getCard());

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.HASTE)).isFalse();
        declareAttackers(player1, List.of());
        assertThat(disciple.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Removing Hellraiser Goblin's abilities ends its attack requirement")
    void losingAbilitiesEndsAttackRequirement() {
        Permanent goblin = addCreatureReady(player1, new HellraiserGoblin());
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.HASTE)).isFalse();
        declareAttackers(player1, List.of());
        assertThat(goblin.isAttacking()).isFalse();
        assertThat(disciple.isAttacking()).isFalse();
    }
}

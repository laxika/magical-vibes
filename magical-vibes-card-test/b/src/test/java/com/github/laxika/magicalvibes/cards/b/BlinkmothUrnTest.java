package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlinkmothUrn.class, Ornithopter.class})
class BlinkmothUrnTest extends BaseCardTest {

    @Test
    @DisplayName("Adds colorless mana for each artifact the active player controls")
    void addsManaForControlledArtifacts() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds the mana to the player whose first main phase it is")
    void addsManaToActivePlayer() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Does not trigger while the urn is tapped")
    void doesNotTriggerWhileTapped() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());
        urn.tap();

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Does not add mana if the urn becomes tapped before the trigger resolves")
    void doesNotAddManaIfTappedBeforeResolution() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(1);

        urn.tap();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void countsArtifactsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        advanceToPrecombatMain(player1);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void addsNoManaToAnActivePlayerWithNoArtifacts() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        advanceToPrecombatMain(player2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void eachUrnAddsManaSeparately() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.addToBattlefield(player2, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());
        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void usesLastKnownUntappedStateWhenUrnLeavesBeforeResolution() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());
        advanceToPrecombatMain(player1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, urn);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void usesLastKnownTappedStateWhenUrnLeavesBeforeResolution() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new Ornithopter());
        advanceToPrecombatMain(player1);

        urn.tap();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, urn);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void doesNotTriggerAtTheBeginningOfPostcombatMain() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}

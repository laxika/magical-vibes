package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GoblinRockSled;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcGeneral.class, GoblinRockSled.class, Squire.class})
class OrcGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Orc or Goblin boosts other Orcs on all battlefields")
    void activationSacrificesAnotherOrcOrGoblinAndBoostsOtherOrcs() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());
        Permanent sacrificed = addCreatureReady(player1, new GoblinRockSled());
        Permanent allyOrc = addCreatureReady(player1, new OrcGeneral());
        Permanent nonOrc = addCreatureReady(player1, new Squire());
        Permanent nonOrcGoblin = addCreatureReady(player1, new GoblinRockSled());
        Permanent opponentOrc = addCreatureReady(player2, new OrcGeneral());

        harness.activateAbility(player1, battlefieldIndex(general), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(sacrificed.getId(), allyOrc.getId(), nonOrcGoblin.getId());
        assertThat(choice.validIds()).doesNotContain(general.getId(), nonOrc.getId());

        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(general.isTapped()).isTrue();
        assertThat(general.getPowerModifier()).isZero();
        assertThat(allyOrc.getPowerModifier()).isEqualTo(1);
        assertThat(allyOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentOrc.getPowerModifier()).isEqualTo(1);
        assertThat(opponentOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(nonOrc.getPowerModifier()).isZero();
        assertThat(nonOrc.getToughnessModifier()).isZero();
        assertThat(nonOrcGoblin.getPowerModifier()).isZero();
        assertThat(nonOrcGoblin.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(allyOrc.getPowerModifier()).isZero();
        assertThat(opponentOrc.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without another Orc or Goblin to sacrifice")
    void activationRequiresAnotherOrcOrGoblin() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(general), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void activationRequiresUntappedSource() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());
        addCreatureReady(player1, new GoblinRockSled());
        general.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(general), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({OrcGeneral.class, BoggartShenanigans.class})
    void canSacrificeNoncreatureGoblin() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        Permanent otherOrc = addCreatureReady(player2, new OrcGeneral());

        harness.activateAbility(player1, battlefieldIndex(general), null, null);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goblin);
        assertThat(general.isTapped()).isTrue();
        assertThat(otherOrc.getPowerModifier()).isEqualTo(1);
        assertThat(otherOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(general.getPowerModifier()).isZero();
    }

    @Test
    void canSacrificeAnotherOrcAndDoesNotBoostLaterEntrants() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());
        Permanent sacrificed = addCreatureReady(player1, new OrcGeneral());
        Permanent otherOrc = addCreatureReady(player2, new OrcGeneral());

        harness.activateAbility(player1, battlefieldIndex(general), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(otherOrc.getPowerModifier()).isZero();

        harness.passBothPriorities();
        Permanent laterOrc = addCreatureReady(player1, new OrcGeneral());

        assertThat(otherOrc.getPowerModifier()).isEqualTo(1);
        assertThat(otherOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(laterOrc.getPowerModifier()).isZero();
        assertThat(laterOrc.getToughnessModifier()).isZero();
    }

    @Test
    void cannotSacrificeOpponentsGoblin() {
        Permanent general = addCreatureReady(player1, new OrcGeneral());
        addCreatureReady(player2, new GoblinRockSled());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(general), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void activationRequiresSourceWithoutSummoningSickness() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new OrcGeneral());
        addCreatureReady(player1, new GoblinRockSled());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(general), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}


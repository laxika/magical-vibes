package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExtruderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a +1/+1 counter on target creature")
    void sacrificingArtifactPutsCounterOnTargetCreature() {
        addReadyExtruder(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Extruder");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyExtruder(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Declining echo sacrifices Extruder at its next upkeep")
    void decliningEchoSacrificesExtruder() {
        castAndResolveExtruder();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Extruder");
        harness.assertInGraveyard(player1, "Extruder");
    }

    @Test
    @DisplayName("Paying echo keeps Extruder and echo does not trigger again")
    void payingEchoKeepsExtruderAndIsOneShot() {
        castAndResolveExtruder();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Extruder");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Extruder");
    }

    private void castAndResolveExtruder() {
        harness.setHand(player1, java.util.List.of(new Extruder()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyExtruder(Player player) {
        Permanent extruder = new Permanent(new Extruder());
        extruder.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(extruder);
        return extruder;
    }
}

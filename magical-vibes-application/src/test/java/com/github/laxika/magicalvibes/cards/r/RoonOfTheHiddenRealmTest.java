package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoonOfTheHiddenRealm.class, GrizzlyBears.class, Quicksand.class})
class RoonOfTheHiddenRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Ability exiles another target creature")
    void exilesAnotherTargetCreature() {
        addReadyRoon();
        harness.addToBattlefield(player2, new GrizzlyBears());
        addRoonMana();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiled creature returns at the next end step under its owner's control")
    void returnsAtNextEndStep() {
        addReadyRoon();
        harness.addToBattlefield(player2, new GrizzlyBears());
        addRoonMana();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target Roon itself")
    void cannotTargetSelf() {
        addReadyRoon();
        addRoonMana();

        UUID roonId = harness.getPermanentId(player1, "Roon of the Hidden Realm");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, roonId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyRoon();
        harness.addToBattlefield(player2, new Quicksand());
        addRoonMana();

        UUID quicksandId = harness.getPermanentId(player2, "Quicksand");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, quicksandId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyRoon() {
        addCreatureReady(player1, new RoonOfTheHiddenRealm());
    }

    private void addRoonMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

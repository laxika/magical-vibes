package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Infest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Deathgreeter.class, GrizzlyBears.class, Shock.class, Infest.class})
class DeathgreeterTest extends BaseCardTest {

    // "Whenever another creature dies, you may gain 1 life."

    @Test
    @DisplayName("Accepting the trigger when another creature dies gains 1 life")
    void anotherCreatureDeathAcceptGainsLife() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve the queued may trigger (awaits choice)

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void anotherCreatureDeathDeclineGainsNoLife() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's creature dying still triggers the may-gain-life ability")
    void opponentCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Deathgreeter dying does not trigger its own ability (another creature only)")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID deathgreeterId = harness.getPermanentId(player1, "Deathgreeter");
        harness.castAndResolveInstant(player2, 0, deathgreeterId);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deathgreeters dying together each trigger for the other creature")
    void simultaneousDeathsTriggerForEachOther() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.castFromHand(player2, new Infest(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A queued life-gain trigger survives Deathgreeter leaving the battlefield")
    void triggerResolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Deathgreeter"));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

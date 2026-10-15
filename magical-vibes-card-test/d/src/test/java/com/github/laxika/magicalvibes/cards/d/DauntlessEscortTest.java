package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.cards.t.Terminate;
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

@CardUsed({DauntlessEscort.class, QasaliPridemage.class, Terminate.class})
class DauntlessEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing grants indestructible to your other creatures and removes itself")
    void sacrificeGrantsIndestructible() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        // Sacrifice cost paid: the Escort is gone.
        harness.assertNotOnBattlefield(player1, "Dauntless Escort");
    }

    @Test
    @DisplayName("Granted creature survives a destroy effect")
    void grantedCreatureSurvivesDestruction() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Qasali Pridemage");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to creatures an opponent controls")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before creatures gain indestructible")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Dauntless Escort");
        harness.assertInGraveyard(player1, "Dauntless Escort");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain indestructible")
    void creaturesEnteringAfterResolutionAreNotProtected() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Activating in response to removal protects the targeted creature")
    void protectsCreatureInResponseToRemoval() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Qasali Pridemage");
        harness.assertInGraveyard(player1, "Dauntless Escort");
        harness.assertInGraveyard(player2, "Terminate");
    }

    @Test
    @DisplayName("Creatures entering before resolution gain indestructible")
    void creaturesEnteringBeforeResolutionAreProtected() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        harness.activateAbility(player1, 0, null, null);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new QasaliPridemage());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An indestructible Escort can still be sacrificed")
    void indestructibleDoesNotPreventSacrifice() {
        harness.addToBattlefield(player1, new DauntlessEscort());
        Permanent secondEscort = harness.addToBattlefieldAndReturn(player1, new DauntlessEscort());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, secondEscort, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dauntless Escort");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}

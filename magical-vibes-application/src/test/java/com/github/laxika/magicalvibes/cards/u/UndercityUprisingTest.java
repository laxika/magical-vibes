package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityUprising.class, HornedTurtle.class, GrizzlyBears.class, HillGiant.class})
class UndercityUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures gain deathtouch before the targeted creatures fight")
    void grantsDeathtouchBeforeFight() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castUndercityUprising("Horned Turtle", "Hill Giant");

        harness.assertOnBattlefield(player1, "Horned Turtle");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");

        var turtle = findPermanent(player1, "Horned Turtle");
        var bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, turtle, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOff() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        castUndercityUprising("Horned Turtle", "Hill Giant");

        var turtle = findPermanent(player1, "Horned Turtle");
        assertThat(gqs.hasKeyword(gd, turtle, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, turtle, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new UndercityUprising()));
        addMana();

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearsId, opponentGiantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UndercityUprising()));
        addMana();

        UUID turtleId = harness.getPermanentId(player1, "Horned Turtle");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(turtleId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("One missing fight target prevents the fight but not the deathtouch grant")
    void oneMissingTargetStillGrantsDeathtouch(boolean removeOwnCreature) {
        var turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UndercityUprising()));
        addMana();
        harness.castSorcery(player1, 0, List.of(turtle.getId(), giant.getId()));

        var removed = removeOwnCreature ? turtle : giant;
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        if (removeOwnCreature) {
            harness.assertOnBattlefield(player2, "Hill Giant");
            assertThat(giant.getMarkedDamage()).isZero();
            assertThat(gqs.hasKeyword(gd, giant, Keyword.DEATHTOUCH)).isFalse();
        } else {
            harness.assertOnBattlefield(player1, "Horned Turtle");
            assertThat(turtle.getMarkedDamage()).isZero();
            assertThat(gqs.hasKeyword(gd, turtle, Keyword.DEATHTOUCH)).isTrue();
        }
        harness.assertInGraveyard(player1, "Undercity Uprising");
    }

    @Test
    @DisplayName("No creatures gain deathtouch when both targets are illegal")
    void bothMissingTargetsPreventDeathtouchGrant() {
        var turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UndercityUprising()));
        addMana();
        harness.castSorcery(player1, 0, List.of(turtle.getId(), giant.getId()));

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turtle);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, giant);
        });
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
        harness.assertInGraveyard(player1, "Undercity Uprising");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain deathtouch")
    void laterCreaturesDoNotGainDeathtouch() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        var opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castUndercityUprising("Horned Turtle", "Hill Giant");

        var laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, laterBears, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Horned Turtle"), Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Both creatures deal fight damage even when both will die")
    void fightCanKillBothCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castUndercityUprising("Grizzly Bears", "Hill Giant");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }
    private void castUndercityUprising(String firstTargetName, String secondTargetName) {
        harness.setHand(player1, List.of(new UndercityUprising()));
        addMana();

        UUID firstTargetId = harness.getPermanentId(player1, firstTargetName);
        UUID secondTargetId = harness.getPermanentId(player2, secondTargetName);
        harness.castAndResolveSorcery(player1, 0, List.of(firstTargetId, secondTargetId));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

}

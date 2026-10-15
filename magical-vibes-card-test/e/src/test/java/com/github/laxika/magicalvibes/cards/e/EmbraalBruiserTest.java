package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbraalBruiser.class, ConsulateSkygate.class})
class EmbraalBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped without menace when you control no artifact")
    void entersTappedWithoutMenace() {
        harness.setHand(player1, List.of(new EmbraalBruiser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bruiser = findPermanent(player1, "Embraal Bruiser");
        assertThat(bruiser.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace while you control an artifact")
    void hasMenaceWhileControllingArtifact() {
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new ConsulateSkygate());

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Loses menace when you stop controlling artifacts")
    void losesMenaceWhenArtifactLeaves() {
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new ConsulateSkygate());

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Consulate Skygate"));

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant menace")
    void opponentArtifactDoesNotGrantMenace() {
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new EmbraalBruiser());
        harness.addToBattlefield(player2, new ConsulateSkygate());

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Still enters tapped when its controller already controls an artifact")
    void entersTappedWithArtifact() {
        harness.addToBattlefield(player1, new ConsulateSkygate());

        Permanent bruiser = harness.enterBattlefieldAndReturn(player1, new EmbraalBruiser());

        assertThat(bruiser.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Gains menace immediately when an artifact enters after it")
    void gainsMenaceWhenArtifactEnters() {
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new EmbraalBruiser());
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new ConsulateSkygate());

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Keeps menace until the last controlled artifact leaves")
    void keepsMenaceWhileAnotherArtifactRemains() {
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new EmbraalBruiser());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ConsulateSkygate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ConsulateSkygate());

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking")
    void cannotBeBlockedByOneCreatureWithArtifact() {
        addCreatureReady(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new ConsulateSkygate());
        addCreatureReady(player2, new ConsulateSkygate());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block while it has menace")
    void canBeBlockedByTwoCreaturesWithArtifact() {
        addCreatureReady(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new ConsulateSkygate());
        addCreatureReady(player2, new ConsulateSkygate());
        addCreatureReady(player2, new ConsulateSkygate());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Embraal Bruiser");
    }

    @Test
    @DisplayName("A single creature can block when only the opponent controls artifacts")
    void canBeBlockedByOneCreatureWithoutOwnArtifact() {
        addCreatureReady(player1, new EmbraalBruiser());
        addCreatureReady(player2, new ConsulateSkygate());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Embraal Bruiser");
    }
}

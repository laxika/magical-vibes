package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepelTheDarkness.class, NestInvader.class, Island.class})
class RepelTheDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures and draws a card")
    void tapsUpToTwoCreaturesAndDraws() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Repel the Darkness");
    }

    @Test
    @DisplayName("Can resolve with no creature targets and still draws a card")
    void canResolveWithoutTargets() {
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can tap exactly one creature controlled by the caster")
    void tapsOneOwnCreatureAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("An already tapped creature is a legal target and the spell still draws")
    void tappedTargetStillAllowsDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        creature.tap();
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Still taps the remaining target and draws when one target leaves")
    void resolvesWithOneRemainingTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.setGraveyard(player2, List.of(first.getCard()));
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Repel the Darkness");
    }

    @Test
    @DisplayName("Does not draw when all chosen targets leave before resolution")
    void doesNotDrawWithAllTargetsIllegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        harness.setHand(player1, List.of(new RepelTheDarkness()));
        Island draw = new Island();
        harness.setLibrary(player1, List.of(draw));
        addMana();

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.setGraveyard(player2, List.of(first.getCard(), second.getCard()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Repel the Darkness");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}

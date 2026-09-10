package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokenDam.class, ShuFootSoldiers.class, ShuCavalry.class, Island.class})
class BrokenDamTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures without horsemanship")
    void tapsTwoTargetCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a single target creature")
    void tapsSingleTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with horsemanship")
    void cannotTargetCreatureWithHorsemanship() {
        Permanent horseman = harness.addToBattlefieldAndReturn(player2, new ShuCavalry());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(horseman.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature without horsemanship");
    }

    @Test
    @DisplayName("Can target creatures controlled by either player")
    void canTargetCreaturesControlledByEitherPlayer() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0,
                List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature without horsemanship");
    }

    @Test
    @DisplayName("Cannot include a creature with horsemanship among two targets")
    void cannotIncludeCreatureWithHorsemanshipAmongTwoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        Permanent horseman = harness.addToBattlefieldAndReturn(player2, new ShuCavalry());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(creature.getId(), horseman.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature without horsemanship");
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("All targets must be different");
    }

    @Test
    @DisplayName("Can target an already tapped creature")
    void canTargetAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        creature.tap();
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }
}

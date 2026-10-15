package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderManWebSlinger.class, Plains.class})
class SpiderManWebSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {W} by returning a tapped creature you control")
    void castsForWebSlingingCost() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new SpiderManWebSlinger());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man, Web-Slinger");
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Web-slinging rejects an untapped creature")
    void requiresTappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new SpiderManWebSlinger());
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Web-slinging cannot return an opponent's creature")
    void requiresCreatureYouControl() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SpiderManWebSlinger());
        opponentCreature.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayNormalManaCostWithoutReturningACreature() {
        harness.castFromHand(player1, new SpiderManWebSlinger(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man, Web-Slinger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void returnsCreatureAsACostBeforeSpellResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpiderManWebSlinger());
        creature.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spider-Man, Web-Slinger");
    }

    @Test
    void requiresOneCreatureToReturn() {
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Spider-Man, Web-Slinger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnATappedNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        harness.assertInHand(player1, "Spider-Man, Web-Slinger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotReplaceWhiteManaWithColorlessMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpiderManWebSlinger());
        creature.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInHand(player1, "Spider-Man, Web-Slinger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsBorrowedCreatureToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpiderManWebSlinger());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        creature.tap();
        harness.setHand(player1, List.of(new SpiderManWebSlinger()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spider-Man, Web-Slinger");
    }
}

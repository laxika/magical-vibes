package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swelter.class, GiantWarthog.class, SuntailHawk.class, KrosanVerge.class})
class SwelterTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals 2 damage to each of two target creatures")
    void damagesBothTargets() {
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        Permanent otherWarthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(warthog.getId(), otherWarthog.getId()));

        assertThat(warthog.getMarkedDamage()).isEqualTo(2);
        assertThat(otherWarthog.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Warthog");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Warthog"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Two damage destroys a 1/1 target while marking damage on a larger target")
    void damagesLethallyAndNonLethally() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(hawk.getId(), warthog.getId()));

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(warthog.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresTwoCreatureTargets() {
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(warthog.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects more than two creature targets")
    void rejectsMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(warthog.getId(), warthog.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target one creature controlled by each player")
    void targetsCreaturesControlledByEitherPlayer() {
        Permanent ownWarthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        Permanent opposingWarthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        harness.castAndResolveSorcery(
                player1, 0, List.of(ownWarthog.getId(), opposingWarthog.getId()));

        assertThat(ownWarthog.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingWarthog.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.addToBattlefield(player1, new KrosanVerge());
        UUID landId = harness.getPermanentId(player1, "Krosan Verge");
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), landId)))
                .isInstanceOf(IllegalStateException.class);
    }
}

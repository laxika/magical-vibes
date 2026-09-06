package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pyroclasm.class, GrizzlyBears.class, GiantSpider.class, PaladinEnVec.class})
class PyroclasmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pyroclasm puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Pyroclasm destroys creatures with toughness 2 or less on both sides")
    void destroysCreaturesWithToughnessTwoOrLess() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pyroclasm does not destroy creatures with toughness greater than 2")
    void doesNotDestroyCreaturesWithToughnessGreaterThanTwo() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Pyroclasm does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Pyroclasm does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Creatures with protection from red survive Pyroclasm")
    void protectionFromRedSurvives() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Paladin en-Vec");
    }

    @Test
    @CardUsed(DarksteelSentinel.class)
    @DisplayName("Indestructible creatures survive Pyroclasm")
    void indestructibleCreaturesSurvive() {
        harness.addToBattlefield(player2, new DarksteelSentinel());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
    }

    @Test
    @DisplayName("Cannot cast Pyroclasm without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}


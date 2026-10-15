package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.f.ForbiddenFriendship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MutualDestruction;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlitzOfTheThunderRaptor.class, ChandraBoldPyromancer.class, GrizzlyBears.class,
        MagmaJet.class, Mountain.class, Shock.class, AlmightyBrushwagg.class,
        ForbiddenFriendship.class, MutualDestruction.class})
class BlitzOfTheThunderRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controller's instant and sorcery cards in their graveyard")
    void dealsDamageForInstantAndSorceryCardsInOwnGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles a creature that would die from its damage")
    void exilesCreatureInsteadOfPuttingItIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can target and exile a planeswalker")
    void exilesPlaneswalkerInsteadOfPuttingItIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 2);
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        harness.assertNotInGraveyard(player2, "Chandra, Bold Pyromancer");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void countsSorceriesButNotCreaturesOrLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new BlitzOfTheThunderRaptor(), new ForbiddenFriendship(),
                new AlmightyBrushwagg(), new Mountain()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void countsGraveyardAtResolutionRatherThanCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new ForbiddenFriendship()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertNotInGraveyard(player2, "Almighty Brushwagg");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void zeroDamageStillExilesTargetSacrificedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor(), new MutualDestruction()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(target.getMarkedDamage()).isZero();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, other.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertNotInGraveyard(player1, "Almighty Brushwagg");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    void exileReplacementExpiresAfterTheTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor(), new Shock()));
        harness.setLibrary(player2, List.of(new Mountain()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

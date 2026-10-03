package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SpellBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleaverBlow.class, GrizzlyBears.class, HillGiant.class, ScatheZombies.class, SpellBlast.class})
class CleaverBlowTest extends BaseCardTest {

    @Test
    @DisplayName("The printed mode destroys a small nonblack opposing creature, draws for both controllers, and creates a tapped Spirit")
    void printedMode() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing every target restriction permits destroying your own large creature and removes the extra controller effects")
    void allBracketsRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, 47, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing only the life-loss bracket keeps the two draws but causes no life loss")
    void lifeLossBracketRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 16, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The printed mode cannot target your own creature or a creature with mana value greater than three")
    void printedModeTargetRestrictions() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new CleaverBlow()));
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"0, 1, 18, 18, 1, true", "1, 2, 18, 18, 1, true",
            "2, 2, 18, 18, 1, true", "8, 2, 18, 20, 0, true",
            "16, 2, 20, 20, 1, true", "32, 2, 18, 18, 1, false",
            "63, 7, 20, 20, 0, false"})
    void individualBracketChoices(int mode, int genericMana, int casterLife, int opponentLife,
                                 int opponentCards, boolean tapped) {
        var targetController = mode == 63 ? player1 : player2;
        Permanent target = harness.addToBattlefieldAndReturn(targetController,
                mode == 1 || mode == 63 ? new ScatheZombies()
                        : mode == 2 ? new HillGiant() : new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);

        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(targetController, target.getCard().getName());
        harness.assertLife(player1, casterLife);
        harness.assertLife(player2, opponentLife);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentCards);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanent(player1, "Spirit").isTapped()).isEqualTo(tapped);
    }

    @Test
    void printedModeCannotTargetBlackCreature() {
        Permanent zombies = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, zombies.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multicleaveDoesNotIncreaseManaValueForCounterspells() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverBlow()));
        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 16, bears.getId());
        var spellId = gd.stack.getLast().getId();
        harness.castInstant(player2, 0, 2, spellId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cleaver Blow");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }
}

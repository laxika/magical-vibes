package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.cards.d.DenProtector;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshasaGravecaller.class, GrizzlyBears.class, DeathWind.class, DenProtector.class})
class RakshasaGravecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not create Zombie tokens")
    void decliningExploitDoesNothing() {
        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertOnBattlefield(player1, "Rakshasa Gravecaller");
    }

    @Test
    @DisplayName("Exploiting a creature creates two Zombie tokens")
    void exploitCreatesTwoZombies() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rakshasa Gravecaller");
    }

    @Test
    @DisplayName("Exploiting itself creates two untapped 2/2 black Zombie creature tokens")
    void exploitingItselfCreatesZombies() {
        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Rakshasa Gravecaller"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rakshasa Gravecaller");
        harness.assertNotOnBattlefield(player1, "Rakshasa Gravecaller");
        assertThat(findPermanents(player1, "Zombie")).hasSize(2).allSatisfy(zombie -> {
            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
            assertThat(zombie.getEffectivePower()).isEqualTo(2);
            assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
            assertThat(zombie.isTapped()).isFalse();
        });
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Removing Gravecaller before exploit resolves allows sacrifice but creates no tokens")
    void removedSourceDoesNotCreateZombies() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DenProtector());
        castGravecallerToExploitTrigger();

        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castInstant(player2, 0, 6, harness.getPermanentId(player1, "Rakshasa Gravecaller"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rakshasa Gravecaller");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Den Protector");
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("The token trigger resolves even if Gravecaller dies after exploiting")
    void tokenTriggerSurvivesSourceRemoval() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DenProtector());
        castRakshasaGravecaller();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castInstant(player2, 0, 6, harness.getPermanentId(player1, "Rakshasa Gravecaller"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rakshasa Gravecaller");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Den Protector");
    }

    private void castGravecallerToExploitTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RakshasaGravecaller(), "{4}{B}");
        harness.passBothPriorities();
    }

    private void castRakshasaGravecaller() {
        castGravecallerToExploitTrigger();
        harness.passBothPriorities();
    }
}

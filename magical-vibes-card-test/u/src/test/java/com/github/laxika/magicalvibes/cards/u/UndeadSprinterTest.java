package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndeadSprinter.class, GrizzlyBears.class, Shock.class, WalkingCorpse.class})
class UndeadSprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without a counter when cast from hand")
    void castFromHandEntersWithoutCounter() {
        harness.setHand(player1, List.of(new UndeadSprinter()));
        addSprinterMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sprinter = findPermanent(player1, "Undead Sprinter");
        assertThat(sprinter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can be cast from the graveyard after a non-Zombie creature died")
    void castFromGraveyardAfterNonZombieCreatureDied() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new UndeadSprinter()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        addSprinterMana();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent sprinter = findPermanent(player1, "Undead Sprinter");
        assertThat(sprinter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard when only a Zombie creature died")
    void zombieDeathDoesNotEnableGraveyardCast() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new UndeadSprinter()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, zombie.getId());
        addSprinterMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard when no creature died")
    void cannotCastFromGraveyardWithoutNonZombieDeath() {
        harness.setGraveyard(player1, List.of(new UndeadSprinter()));
        addSprinterMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("A non-Zombie death still enables casting when a Zombie also died")
    void mixedCreatureDeathsEnableGraveyardCast() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new UndeadSprinter()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, zombie.getId());
        addSprinterMana();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Undead Sprinter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Undead Sprinter");
    }

    @Test
    @DisplayName("Your own non-Zombie creature dying enables graveyard casting")
    void ownNonZombieDeathEnablesGraveyardCast() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UndeadSprinter sprinterCard = new UndeadSprinter();
        harness.setGraveyard(player1, List.of(sprinterCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        addSprinterMana();
        harness.castFromGraveyard(player1, sprinterCard.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Undead Sprinter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Zombie death does not add a counter when cast from hand")
    void handCastAfterNonZombieDeathStillEntersWithoutCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new UndeadSprinter()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        addSprinterMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Undead Sprinter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addSprinterMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

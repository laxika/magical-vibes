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

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, zombie.getId());
        harness.passBothPriorities();
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

    private void addSprinterMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

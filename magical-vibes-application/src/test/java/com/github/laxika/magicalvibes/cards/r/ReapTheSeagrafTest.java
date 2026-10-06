package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReapTheSeagraf.class})
class ReapTheSeagrafTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Reap the Seagraf creates one 2/2 black Zombie token")
    void createsOneZombieToken() {
        harness.setHand(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);

        Permanent zombie = zombies.getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Normal cast puts Reap the Seagraf into graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reap the Seagraf");
    }

    @Test
    @DisplayName("Flashback creates one Zombie token")
    void flashbackCreatesOneZombieToken() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Flashback exiles Reap the Seagraf after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Reap the Seagraf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reap the Seagraf"));
    }

    @Test
    @DisplayName("Flashback puts Reap the Seagraf on the stack as a sorcery")
    void flashbackPutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reap the Seagraf");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One card creates a Zombie normally and another through flashback")
    void normalCastThenFlashbackCreatesTwoZombies() {
        harness.setHand(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Reap the Seagraf");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
        harness.assertNotInGraveyard(player1, "Reap the Seagraf");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reap the Seagraf"));
    }

    @Test
    @DisplayName("Flashback requires blue mana even with enough total mana")
    void flashbackFailsWithoutBlueMana() {
        harness.setGraveyard(player1, List.of(new ReapTheSeagraf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Reap the Seagraf");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}

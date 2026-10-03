package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineRiders.class, LlanowarElves.class, GrizzlyBears.class})
class WolverineRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elf Warrior token at the beginning of each upkeep")
    void createsTokenOnEachUpkeep() {
        harness.addToBattlefield(player1, new WolverineRiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
                });
    }

    @Test
    @DisplayName("Gains life equal to the toughness of another entering Elf")
    void gainsLifeForEnteringElf() {
        harness.addToBattlefield(player1, new WolverineRiders());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life for a non-Elf entering creature")
    void doesNotGainLifeForNonElf() {
        harness.addToBattlefield(player1, new WolverineRiders());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when Wolverine Riders enters")
    void doesNotGainLifeForSelfEntry() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WolverineRiders()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }
}

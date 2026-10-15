package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingHunter.class, SnarlingWolf.class})
class FlourishingHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains life equal to the greatest toughness among other creatures you control")
    void gainsLifeForGreatestOtherCreatureToughness() {
        SnarlingWolf other = new SnarlingWolf();
        other.setToughness(4);
        harness.addToBattlefield(player1, other);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("ETB gains no life when there are no other creatures you control")
    void gainsNoLifeWithoutOtherCreatures() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB uses the greatest toughness rather than the total, and counts another Hunter")
    void countsAnotherHunterAndUsesMaximumToughness() {
        harness.addToBattlefield(player1, new SnarlingWolf());
        harness.addToBattlefield(player1, new FlourishingHunter());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("ETB ignores creatures controlled by the opponent")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player1, new SnarlingWolf());
        harness.addToBattlefield(player2, new FlourishingHunter());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("ETB checks current toughness when the trigger resolves")
    void usesToughnessAfterResponseResolves() {
        harness.addToBattlefield(player1, new SnarlingWolf());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new FlourishingHunter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
    }
}

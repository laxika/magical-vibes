package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.ScrybRanger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingDenial.class, ScrybRanger.class, Cancel.class})
class OverwhelmingDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when cast normally")
    void countersSpellNormally() {
        ScrybRanger target = new ScrybRanger();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new OverwhelmingDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scryb Ranger");
        harness.assertInGraveyard(player2, "Overwhelming Denial");
    }

    @Test
    @DisplayName("Casts for its surge cost after another spell was cast")
    void castsForSurgeCost() {
        ScrybRanger ownSpell = new ScrybRanger();
        ScrybRanger target = new ScrybRanger();
        harness.setHand(player1, List.of(ownSpell, new OverwhelmingDenial()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Scryb Ranger");
        harness.assertInGraveyard(player1, "Overwhelming Denial");
    }

    @Test
    @DisplayName("Cannot cast for its surge cost before another spell was cast")
    void surgeCostRequiresAnotherSpell() {
        ScrybRanger target = new ScrybRanger();
        harness.setHand(player1, List.of(new OverwhelmingDenial()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        ScrybRanger target = new ScrybRanger();
        OverwhelmingDenial denial = new OverwhelmingDenial();
        harness.setHand(player1, List.of(denial));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(target, new Cancel()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, denial.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scryb Ranger");
        harness.assertInGraveyard(player1, "Overwhelming Denial");
        harness.assertInGraveyard(player2, "Cancel");
    }
}

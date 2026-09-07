package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PumpkinBombardment.class, Forest.class, GrizzlyBears.class})
class PumpkinBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and deals 3 damage to target creature")
    void discardsCardAndDealsThreeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castPumpkinBombardment(target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pays {2} instead of discarding and deals 3 damage")
    void paysManaInsteadOfDiscarding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castPumpkinBombardment(target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a discard or enough mana for the alternate cost")
    void cannotCastWithoutDiscardOrMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> castPumpkinBombardment(target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card or pay {2}");
    }

    @Test
    @DisplayName("Rejects a player as the target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new PumpkinBombardment(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> castPumpkinBombardment(player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPumpkinBombardment(UUID targetId, Integer discardHandCardIndex) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, discardHandCardIndex);
    }
}

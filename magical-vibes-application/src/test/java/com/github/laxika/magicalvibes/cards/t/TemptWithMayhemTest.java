package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptWithMayhem.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class TemptWithMayhemTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a controller copy and rewards an accepting opponent with another controller copy")
    void acceptingOpponentCreatesCopiesForBothPlayers() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player1.getId())).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional copy")
    void decliningOpponentLeavesOnlyBaseCopy() {
        CounselOfTheSoratami counsel = castCounsel();

        castTemptWithMayhem(counsel);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(entry -> entry.isCopy()
                && entry.getControllerId().equals(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.setHand(player1, List.of(new TemptWithMayhem()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private CounselOfTheSoratami castCounsel() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0);
        return counsel;
    }

    private void castTemptWithMayhem(CounselOfTheSoratami counsel) {
        harness.setHand(player1, List.of(new TemptWithMayhem()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, counsel.getId());
        harness.passBothPriorities();
    }
}

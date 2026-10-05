package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzzetLocket.class, VernadiShieldmate.class})
class IzzetLocketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Izzet Locket adds blue or red mana")
    void tappingAddsChosenMana() {
        Permanent locket = addReadyLocket();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four hybrid mana sacrifices Izzet Locket and draws two cards")
    void payingHybridManaSacrificesAndDrawsTwo() {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new VernadiShieldmate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof VernadiShieldmate)
                .hasSize(2);
    }

    @Test
    void newlyEnteredLocketCanProduceBlueMana() {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new IzzetLocket());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hybridCostCanBePaidEntirelyWithBlue() {
        assertSingleColorPayment(ManaColor.BLUE);
    }

    @Test
    void hybridCostCanBePaidEntirelyWithRed() {
        assertSingleColorPayment(ManaColor.RED);
    }

    private void assertSingleColorPayment(ManaColor color) {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new IzzetLocket());
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new VernadiShieldmate()));
        harness.addMana(player1, color, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void tappedLocketCannotActivateEitherAbility() {
        Permanent locket = addReadyLocket();
        locket.tap();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wrongColorManaCannotPayHybridCost() {
        assertRejectedPayment(ManaColor.GREEN, 4);
    }

    @Test
    void insufficientManaCannotPayHybridCost() {
        assertRejectedPayment(ManaColor.BLUE, 3);
    }

    private void assertRejectedPayment(ManaColor color, int amount) {
        Permanent locket = addReadyLocket();
        harness.addMana(player1, color, amount);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(locket.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(amount);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLocket() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new IzzetLocket());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariLocket.class, BartizanBats.class})
class GolgariLocketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Golgari Locket adds black or green mana")
    void tappingAddsChosenMana() {
        Permanent locket = addReadyLocket();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four hybrid mana sacrifices Golgari Locket and draws two cards")
    void payingHybridManaSacrificesAndDrawsTwo() {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof BartizanBats)
                .hasSize(2);
    }

    private Permanent addReadyLocket() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GolgariLocket());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void newlyEnteredLocketCanProduceBlackMana() {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new GolgariLocket());
        locket.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawAbilityCanBePaidEntirelyWithBlackMana() {
        assertSingleColorPayment(ManaColor.BLACK);
    }

    @Test
    void drawAbilityCanBePaidEntirelyWithGreenMana() {
        assertSingleColorPayment(ManaColor.GREEN);
    }

    private void assertSingleColorPayment(ManaColor color) {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.addMana(player1, color, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void tappedLocketCannotActivateEitherAbility() {
        Permanent locket = addReadyLocket();
        locket.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientHybridManaDoesNotTapOrSacrificeLocket() {
        Permanent locket = addReadyLocket();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(locket.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}

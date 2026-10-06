package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicLocket.class})
class SimicLocketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Simic Locket adds green or blue mana")
    void tappingAddsChosenMana() {
        Permanent locket = addReadyLocket();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four hybrid mana sacrifices Simic Locket and draws two cards")
    void payingHybridManaSacrificesAndDrawsTwo() {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new SimicLocket(), new SimicLocket()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof SimicLocket)
                .hasSize(2);
    }

    @Test
    @DisplayName("A newly controlled noncreature Locket can tap for blue mana")
    void newlyControlledLocketAddsBlueMana() {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new SimicLocket());
        locket.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE"})
    @DisplayName("Four mana of either hybrid color pays for the draw ability")
    void drawAbilityAcceptsEitherColorAlone(ManaColor color) {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new SimicLocket(), new SimicLocket()));
        harness.addMana(player1, color, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE", "COLORLESS", "RED"})
    @DisplayName("Insufficient or wrong-colored mana cannot pay for the draw ability")
    void invalidManaDoesNotSacrificeLocket(ManaColor color) {
        Permanent locket = addReadyLocket();
        int amount = color == ManaColor.GREEN || color == ManaColor.BLUE ? 3 : 4;
        harness.addMana(player1, color, amount);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(locket.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(amount);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Locket cannot activate either ability")
    void tappedLocketCannotActivateEitherAbility() {
        Permanent locket = addReadyLocket();
        locket.tap();
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLocket() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SimicLocket());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

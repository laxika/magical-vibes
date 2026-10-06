package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosLocket.class})
class RakdosLocketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rakdos Locket adds black or red mana")
    void tappingAddsChosenMana() {
        Permanent locket = addReadyLocket();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four hybrid mana sacrifices Rakdos Locket and draws two cards")
    void payingHybridManaSacrificesAndDrawsTwo() {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new RakdosLocket(), new RakdosLocket()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof RakdosLocket)
                .hasSize(2);
    }

    @Test
    void tappingAddsBlackManaOnTheTurnItEnters() {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new RakdosLocket());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    void drawAbilityCanBePaidEntirelyWithEitherColor(ManaColor color) {
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new RakdosLocket());
        harness.setLibrary(player1, List.of(new RakdosLocket(), new RakdosLocket()));
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
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedLocketCannotActivateEitherAbility(int abilityIndex) {
        Permanent locket = addReadyLocket();
        locket.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "COLORLESS", "GREEN"})
    void drawAbilityRejectsInsufficientOrWrongColoredMana(ManaColor color) {
        Permanent locket = addReadyLocket();
        int amount = color == ManaColor.BLACK ? 3 : 4;
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new RakdosLocket());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

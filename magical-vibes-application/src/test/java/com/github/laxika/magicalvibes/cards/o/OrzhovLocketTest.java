package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.ImpassionedOrator;
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

@CardUsed({OrzhovLocket.class, ImpassionedOrator.class})
class OrzhovLocketTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK"})
    @DisplayName("Tapping Orzhov Locket adds white or black mana")
    void tappingAddsChosenMana(ManaColor color) {
        Permanent locket = addReadyLocket();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        ManaColor otherColor = color == ManaColor.WHITE ? ManaColor.BLACK : ManaColor.WHITE;
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four hybrid mana sacrifices Orzhov Locket and draws two cards")
    void payingHybridManaSacrificesAndDrawsTwo() {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new ImpassionedOrator(), new ImpassionedOrator()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof ImpassionedOrator)
                .hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK"})
    void drawAbilityCanBePaidWithOneColor(ManaColor color) {
        Permanent locket = addReadyLocket();
        harness.setLibrary(player1, List.of(new ImpassionedOrator(), new ImpassionedOrator()));
        harness.addMana(player1, color, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(locket.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED", "GREEN", "COLORLESS"})
    void drawAbilityRejectsManaOutsideItsHybridColors(ManaColor color) {
        Permanent locket = addReadyLocket();
        harness.addMana(player1, color, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(locket.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawAbilityRequiresFourMana() {
        Permanent locket = addReadyLocket();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(locket.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedLocketCannotActivateEitherAbility(int abilityIndex) {
        Permanent locket = addReadyLocket();
        locket.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(locket);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locket.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLocket() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OrzhovLocket());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

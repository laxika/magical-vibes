package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosCluestone.class})
class RakdosCluestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Rakdos Cluestone adds black or red mana")
    void tappingAddsChosenMana() {
        Permanent cluestone = addReadyCluestone();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(cluestone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying black and red mana sacrifices Rakdos Cluestone and draws a card")
    void payingBlackAndRedSacrificesAndDraws() {
        Permanent cluestone = addReadyCluestone();
        harness.setLibrary(player1, List.of(new RakdosCluestone()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof RakdosCluestone);
    }

    @Test
    @DisplayName("A newly entered noncreature Cluestone can tap for black mana")
    void newlyEnteredCluestoneAddsBlackMana() {
        Permanent cluestone = harness.addToBattlefieldAndReturn(player1, new RakdosCluestone());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(cluestone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Cluestone cannot activate either ability")
    void tappedCluestoneCannotActivateEitherAbility() {
        Permanent cluestone = addReadyCluestone();
        cluestone.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two black mana cannot pay the black and red draw cost")
    void drawRequiresBothColorsBeforePayingCosts() {
        Permanent cluestone = addReadyCluestone();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cluestone.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cluestone);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cluestone.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCluestone() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new RakdosCluestone());
        permanent.setSummoningSick(false);
        return permanent;
    }
}

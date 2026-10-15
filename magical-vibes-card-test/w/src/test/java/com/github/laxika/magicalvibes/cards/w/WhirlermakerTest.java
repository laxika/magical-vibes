package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Whirlermaker.class})
class WhirlermakerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Whirlermaker creates a flying Thopter artifact creature token")
    void createsThopterToken() {
        Permanent whirler = addWhirlermakerReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(whirler), null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Thopter");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    @DisplayName("Activating Whirlermaker taps it and spends four mana")
    void activationPaysAndTaps() {
        Permanent whirler = addWhirlermakerReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(whirler), null, null);

        assertThat(whirler.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Whirlermaker cannot be activated without four mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent whirler = addWhirlermakerReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(whirler), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped Whirlermaker cannot be activated")
    void cannotActivateWhileTapped() {
        Permanent whirler = addWhirlermakerReady();
        whirler.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(whirler), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Whirlermaker can activate immediately and pay generic costs with colored mana")
    void canActivateImmediatelyWithColoredMana() {
        Permanent whirler = harness.enterBattlefieldAndReturn(player1, new Whirlermaker());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(whirler), null, null);
        assertThat(countPermanents(player1, "Thopter")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The token ability resolves even after Whirlermaker leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent whirler = addWhirlermakerReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, indexOf(whirler), null, null);

        gd.playerBattlefields.get(player1.getId()).remove(whirler);
        gd.playerGraveyards.get(player1.getId()).add(whirler.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability creates a token for its controller on the opponent's turn")
    void createsTokenForOpponentController() {
        Permanent whirler = harness.addToBattlefieldAndReturn(player2, new Whirlermaker());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(whirler), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isZero();
    }

    private Permanent addWhirlermakerReady() {
        return addCreatureReady(player1, new Whirlermaker());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

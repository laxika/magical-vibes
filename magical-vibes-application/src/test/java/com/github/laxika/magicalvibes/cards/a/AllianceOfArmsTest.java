package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllianceOfArms.class, VedalkenOrrery.class})
class AllianceOfArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player creates white Soldiers equal to the total mana paid")
    void eachPlayerPaysManaForSoldiers() {
        harness.setHand(player1, List.of(new AllianceOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 4); // {W} to cast, then 3 for the effect
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // The total of five mana paid is the token count for each player.
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
        assertThat(countPermanents(player2, "Soldier")).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying zero creates no Soldiers and leaves mana unspent")
    void payingZeroCreatesNoSoldiers() {
        harness.setHand(player1, List.of(new AllianceOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("A player without mana is skipped")
    void playerWithoutManaIsSkipped() {
        harness.setHand(player1, List.of(new AllianceOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("A player who declines payment still receives the total number of Soldiers")
    void decliningPlayerStillReceivesSoldiers() {
        harness.setHand(player1, List.of(new AllianceOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(countPermanents(player2, "Soldier")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Payments start with the spell controller on an opponent's turn")
    void paymentsStartWithControllerOnOpponentsTurn() {
        harness.addToBattlefield(player2, new VedalkenOrrery());
        harness.setHand(player2, List.of(new AllianceOfArms()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.handleXValueChosen(player2, 3);
        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
        assertThat(countPermanents(player2, "Soldier")).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}

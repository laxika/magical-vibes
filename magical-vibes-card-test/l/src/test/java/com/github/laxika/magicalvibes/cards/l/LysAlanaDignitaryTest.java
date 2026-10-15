package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LysAlanaDignitary.class})
class LysAlanaDignitaryTest extends BaseCardTest {

    @Test
    void requiresAdditionalManaWithoutAnElf() {
        harness.setHand(player1, List.of(new LysAlanaDignitary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anElfPermanentAvoidsTheAdditionalMana() {
        Card elf = new LysAlanaDignitary();
        harness.addToBattlefield(player1, elf);
        LysAlanaDignitary dignitary = new LysAlanaDignitary();
        harness.setHand(player1, List.of(dignitary));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(dignitary.getId()));
    }

    @Test
    void anElfCardInHandAvoidsTheAdditionalMana() {
        LysAlanaDignitary dignitary = new LysAlanaDignitary();
        Card elf = new LysAlanaDignitary();
        harness.setHand(player1, List.of(dignitary, elf));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(dignitary.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elf.getId()));
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    void canPayAdditionalManaWithoutAnotherElf() {
        harness.setHand(player1, List.of(new LysAlanaDignitary()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lys Alana Dignitary");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void opponentsElfDoesNotAvoidAdditionalMana() {
        harness.addToBattlefield(player2, new LysAlanaDignitary());
        harness.setHand(player1, List.of(new LysAlanaDignitary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsGraveyardElfDoesNotAllowActivation() {
        Permanent dignitary = harness.addToBattlefieldAndReturn(player1, new LysAlanaDignitary());
        dignitary.setSummoningSick(false);
        harness.setGraveyard(player2, List.of(new LysAlanaDignitary()));
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf card in your graveyard");
        assertThat(dignitary.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new LysAlanaDignitary());
        harness.setGraveyard(player1, List.of(new LysAlanaDignitary()));
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addsTwoGreenManaWithAnElfInTheGraveyard() {
        LysAlanaDignitary card = new LysAlanaDignitary();
        Permanent dignitary = harness.addToBattlefieldAndReturn(player1, card);
        dignitary.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new LysAlanaDignitary()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(dignitary.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutAnElfInTheGraveyard() {
        LysAlanaDignitary card = new LysAlanaDignitary();
        Permanent dignitary = harness.addToBattlefieldAndReturn(player1, card);
        dignitary.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf card in your graveyard");
    }
}

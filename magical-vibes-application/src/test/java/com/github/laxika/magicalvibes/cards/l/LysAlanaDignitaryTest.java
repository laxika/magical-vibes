package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Card elf = new ElvishEulogist();
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
        Card elf = new ElvishEulogist();
        harness.setHand(player1, List.of(dignitary, elf));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(dignitary.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elf.getId()));
    }

    @Test
    void addsTwoGreenManaWithAnElfInTheGraveyard() {
        LysAlanaDignitary card = new LysAlanaDignitary();
        Permanent dignitary = new Permanent(card);
        dignitary.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(dignitary);
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(dignitary.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutAnElfInTheGraveyard() {
        LysAlanaDignitary card = new LysAlanaDignitary();
        Permanent dignitary = new Permanent(card);
        dignitary.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(dignitary);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf card in your graveyard");
    }
}

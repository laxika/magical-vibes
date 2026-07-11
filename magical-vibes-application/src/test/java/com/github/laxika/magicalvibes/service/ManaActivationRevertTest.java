package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WanderwineHub;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The MTGO-style "cancel casting" flow: mana-ability activations are recorded in
 * {@code GameData.revertableManaActivations} and {@code GameService.revertManaActivations}
 * undoes them — sources untap, the produced mana leaves the pool — as long as the mana
 * hasn't been spent.
 */
class ManaActivationRevertTest extends BaseCardTest {

    private static Card createGreenCreature() {
        Card card = new Card();
        card.setName("Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Reverting untaps tapped lands and drains the mana they produced")
    void revertUntapsLandsAndDrainsPool() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Reverting an activated mana ability (dual land) untaps it and removes the chosen color")
    void revertActivatedManaAbility() {
        harness.addToBattlefield(player1, new WanderwineHub());
        harness.ensurePriority(player1);

        // Second ability: {T}: Add {U}.
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spent mana is not reverted: after casting with one of two taps, only the unspent land untaps")
    void spentManaIsNotReverted() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(createGreenCreature()));
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.castCreature(player1, 0); // pays {G}, leaving 1 G floating

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream().filter(Permanent::isTapped).count()).isEqualTo(1);
        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Passing priority clears the revert log — nothing untaps afterwards")
    void passPriorityClearsRevertLog() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        harness.tapPermanent(player1, 0);
        assertThat(gd.revertableManaActivations).hasSize(1);

        harness.passPriority(player1);

        assertThat(gd.revertableManaActivations).isEmpty();
    }

    @Test
    @DisplayName("Reverting with nothing recorded is a no-op")
    void revertWithoutActivationsIsNoOp() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);

        gs.revertManaActivations(gd, player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A player without priority cannot revert, and the log is untouched")
    void revertRequiresPriority() {
        harness.addToBattlefield(player1, new Forest());
        harness.ensurePriority(player1);
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> gs.revertManaActivations(gd, player2))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.revertableManaActivations).hasSize(1);
    }
}

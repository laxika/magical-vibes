package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.r.RagingBull;
import com.github.laxika.magicalvibes.cards.r.Recall;
import com.github.laxika.magicalvibes.cards.s.StormSeeker;
import com.github.laxika.magicalvibes.cards.s.SylvanLibrary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaMatrix.class, StormSeeker.class, SylvanLibrary.class, Recall.class, RagingBull.class,
        HolyDay.class})
class ManaMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Instant spells you cast cost {2} less")
    void instantSpellsAreReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchantment spells you cast cost {2} less")
    void enchantmentSpellsAreReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new SylvanLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sorcery and creature spells are not reduced")
    void otherSpellTypesAreNotReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());

        harness.setHand(player1, List.of(new Recall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        gd.playerManaPools.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new RagingBull()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not pay colored mana")
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new HolyDay()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to opponents' spells")
    void opponentsSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player2, List.of(new StormSeeker()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Mana Matrices reduce generic costs cumulatively")
    void multipleMatricesStack() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped Mana Matrix still reduces spell costs")
    void tappedMatrixStillReducesCosts() {
        harness.addToBattlefieldAndReturn(player1, new ManaMatrix()).setTapped(true);
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mana Matrix does not reduce artifact spells")
    void artifactSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new ManaMatrix()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess generic reduction still leaves colored mana to pay")
    void excessReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.addToBattlefield(player1, new ManaMatrix());
        harness.setHand(player1, List.of(new SylvanLibrary()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerrowReejerey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StonybrookBanneretTest extends BaseCardTest {

    // ===== Merfolk cost reduction =====

    @Test
    @DisplayName("Merfolk spells cost {1} less with Stonybrook Banneret on the battlefield")
    void merfolkSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Merrow Reejerey (Merfolk) costs {2}{U} — with {1} reduction it costs {1}{U}, castable with 2 blue
        harness.setHand(player1, List.of(new MerrowReejerey()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Merrow Reejerey");
    }

    @Test
    @DisplayName("Merfolk spell is not castable when mana falls short of the reduced cost")
    void merfolkNotCastableWithoutEnoughMana() {
        // Without the Banneret, Merrow Reejerey costs {2}{U}; two blue is not enough
        harness.setHand(player1, List.of(new MerrowReejerey()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Wizard cost reduction =====

    @Test
    @DisplayName("Wizard spells cost {1} less with Stonybrook Banneret on the battlefield")
    void wizardSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Ghitu Journeymage (Human Wizard) costs {2}{R} — with {1} reduction it costs {1}{R}
        harness.setHand(player1, List.of(new GhituJourneymage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghitu Journeymage");
    }

    // ===== Non-matching spells are not reduced =====

    @Test
    @DisplayName("Non-Merfolk, non-Wizard spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Grizzly Bears (Bear) costs {1}{G} — not reduced; one green is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Stonybrook Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Opponent's Merrow Reejerey still costs {2}{U}; two blue is not enough
        harness.setHand(player2, List.of(new MerrowReejerey()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fruition.class, Forest.class, Island.class, GrizzlyBears.class})
class FruitionTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts sorcery spell on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Fruition(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    // ===== Life gain =====

    @Test
    @DisplayName("Gains 1 life for each Forest on the battlefield, both players")
    void gainsLifePerForestOnBattlefield() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Fruition(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not count non-Forest lands or other permanents")
    void doesNotCountNonForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Fruition(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains no life when no Forests are on the battlefield")
    void gainsNoLifeWithNoForests() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Fruition(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the spell's controller gains the life")
    void onlyControllerGainsLife() {
        harness.addToBattlefield(player2, new Forest());

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Fruition(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count Forests outside the battlefield")
    void doesNotCountForestsOutsideBattlefield() {
        harness.setHand(player1, List.of(new Fruition(), new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}

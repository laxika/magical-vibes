package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BituminousBlastTest extends BaseCardTest {

    // ===== 4 damage to target creature =====

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 3/3")
    void deals4DamageKillingTargetCreature() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant()); // 3/3

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Marks 4 damage on a creature large enough to survive")
    void marks4DamageOnSurvivingCreature() {
        prepareCaster();
        Permanent brontodon = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon()); // 9/9

        harness.castInstant(player1, 0, brontodon.getId());
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        Permanent surviving = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(brontodon.getId()))
                .findFirst().orElseThrow();
        assertThat(surviving.getMarkedDamage()).isEqualTo(4);
    }

    // ===== Cascade =====

    @Test
    @DisplayName("Cascade digs past a land to the first lesser-mana-value nonland")
    void cascadeOffersLesserManaValueNonland() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        // Bituminous Blast is {3}{B}{R} = mana value 5. Dig skips the Mountain and stops at
        // Grizzly Bears (MV 2 < 5).
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Mountain(), new GrizzlyBears()));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCaster();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void prepareCaster() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        // Empty library by default so the cascade trigger is a no-op unless a test stocks it.
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new BituminousBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

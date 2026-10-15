package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TyrantOfValakut.class, GrizzlyBears.class, Shock.class})
class TyrantOfValakutTest extends BaseCardTest {

    @Test
    @DisplayName("Surge ETB deals 3 damage to any target")
    void surgeEtbDealsDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new TyrantOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tyrant of Valakut");
    }

    @Test
    @DisplayName("Normal casting does not trigger the surge ability")
    void normalCastDoesNotTriggerSurge() {
        harness.setHand(player1, List.of(new TyrantOfValakut()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tyrant of Valakut");
    }

    @Test
    @DisplayName("Surge is unavailable before another spell is cast")
    void surgeRequiresAnotherSpell() {
        harness.setHand(player1, List.of(new TyrantOfValakut()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surge ETB can deal exactly 3 damage to a player")
    void surgeEtbCanTargetPlayer() {
        harness.setHand(player1, List.of(new Shock(), new TyrantOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Tyrant of Valakut");
    }

    @Test
    @DisplayName("Casting another spell does not trigger damage when the normal cost is paid")
    void normalCostAfterAnotherSpellDoesNotTriggerDamage() {
        harness.setHand(player1, List.of(new Shock(), new TyrantOfValakut()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Tyrant of Valakut");
    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BronzeTabletTest extends BaseCardTest {

    private boolean inExile(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("Paying 10 life puts Bronze Tablet into its owner's graveyard; the target stays exiled")
    void payingKeepsOwnership() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new BronzeTablet());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities(); // resolve ability -> may-pay prompt for player2

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        // Bronze Tablet leaves exile for its owner's graveyard; the target permanent stays exiled.
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Bronze Tablet"));
        assertThat(inExile("Bronze Tablet")).isFalse();
        assertThat(inExile("Grizzly Bears")).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining to pay leaves both cards exiled (ownership swap not modeled)")
    void decliningExiles() {
        harness.addToBattlefieldAndReturn(player1, new BronzeTablet());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Both remain exiled; Bronze Tablet is not put into a graveyard.
        assertThat(inExile("Bronze Tablet")).isTrue();
        assertThat(inExile("Grizzly Bears")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Bronze Tablet"));
    }

    @Test
    @DisplayName("An owner who can't pay 10 life resolves the ante swap automatically with no prompt")
    void cannotPayResolvesAutomatically() {
        harness.addToBattlefieldAndReturn(player1, new BronzeTablet());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player2, 5);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // No pay prompt is raised — the ante swap resolves on its own.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
        assertThat(inExile("Bronze Tablet")).isTrue();
        assertThat(inExile("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("The ability can't target a permanent the activating player owns")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefieldAndReturn(player1, new BronzeTablet());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nontoken permanent an opponent owns");
    }
}

package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelvaultTest extends BaseCardTest {

    // ===== {1}, {T}: Exile target creature you control =====

    @Test
    @DisplayName("First ability exiles a creature you control, tracked with Helvault")
    void firstAbilityExilesOwnCreature() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(helvault.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("First ability cannot target a creature you don't control")
    void firstAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new Helvault());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {7}, {T}: Exile target creature you don't control =====

    @Test
    @DisplayName("Second ability exiles a creature you don't control, tracked with Helvault")
    void secondAbilityExilesOpponentCreature() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(helvault.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Second ability cannot target a creature you control")
    void secondAbilityCannotTargetOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new Helvault());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Death trigger: return all cards exiled with it =====

    @Test
    @DisplayName("When Helvault dies, all cards exiled with it return under their owners' control")
    void deathReturnsExiledCardsToOwners() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());

        // Exile player1's own creature with the first ability.
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, ownBears.getId());
        harness.passBothPriorities();

        // Untap Helvault so its second {T} ability can be activated this turn.
        helvault.untap();

        // Exile player2's creature with the second ability.
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).hasSize(2);

        // Destroy Helvault — its death trigger goes onto the stack.
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault);
        harness.passBothPriorities();

        // Both creatures return to the battlefield under their owners' control.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Nothing remains tracked with the (now-dead) Helvault.
        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Helvault"));
    }

    @Test
    @DisplayName("Helvault dying with no exiled cards does nothing extra")
    void deathWithNoExiledCards() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Helvault"));
    }
}

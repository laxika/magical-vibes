package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flashback.class, Shock.class, GrizzlyBears.class, LavaAxe.class, BumpInTheNight.class})
class FlashbackTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants flashback to target instant in graveyard")
    void grantsFlashbackToTargetInstant() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, shock.getId());

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Cannot target creature card in graveyard")
    void cannotTargetCreatureInGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flashback allows casting the targeted spell")
    void grantedFlashbackAllowsCasting() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsSorceryFlashbackAtItsManaCostAndExilesIt() {
        LavaAxe axe = new LavaAxe();
        harness.setGraveyard(player1, List.of(axe));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, axe.getId());
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(axe.getId()));
        harness.assertNotInGraveyard(player1, "Lava Axe");
    }

    @Test
    void grantedFlashbackRequiresPayingTheManaCost() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, shock.getId());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void grantedFlashbackDoesNotOverrideSorceryTiming() {
        LavaAxe axe = new LavaAxe();
        harness.setGraveyard(player1, List.of(axe));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castAndResolveInstant(player1, 0, axe.getId());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Lava Axe");
    }

    @Test
    void grantedFlashbackExpiresAtEndOfTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void canUseGrantedCostInsteadOfExistingFlashbackCost() {
        BumpInTheNight bump = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(bump));
        harness.setHand(player1, List.of(new Flashback()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, bump.getId());
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bump.getId()));
    }
}

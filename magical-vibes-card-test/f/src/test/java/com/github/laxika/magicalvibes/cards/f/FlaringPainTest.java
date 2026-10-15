package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.p.PrismaticStrands;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlaringPain.class, PrismaticStrands.class, EmberShot.class})
class FlaringPainTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast makes damage unpreventable for the turn")
    void normalCastMakesDamageUnpreventable() {
        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    @DisplayName("Normal cast overrides a color-based damage prevention effect")
    void normalCastOverridesColorBasedDamagePrevention() {
        harness.castFromHand(player1, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flashback makes damage unpreventable and exiles Flaring Pain")
    void flashbackMakesDamageUnpreventableAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
        harness.assertNotInGraveyard(player1, "Flaring Pain");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flaring Pain"));
    }

    @Test
    @DisplayName("Damage prevention becomes available again at end of turn")
    void damagePreventionLockExpiresAtEndOfTurn() {
        gd.damageCantBePreventedThisTurn = true;

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        assertThat(gqs.isDamagePreventable(gd)).isTrue();
    }

    @Test
    @DisplayName("Prevention cast after Flaring Pain cannot prevent either player's damage")
    void laterPreventionCannotPreventDamageFromEitherPlayer() {
        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();

        harness.castFromHand(player2, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player2, "RED");

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new EmberShot()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flashback overrides an existing prevention effect")
    void flashbackOverridesExistingPrevention() {
        harness.castFromHand(player2, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player2, "RED");

        harness.setGraveyard(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFlashback(player1, 0, null);

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flaring Pain does not undo damage already prevented")
    void doesNotUndoPreviouslyPreventedDamage() {
        harness.castFromHand(player2, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player2, "RED");

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);

        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("New damage prevention works on the following turn")
    void preventionWorksOnFollowingTurn() {
        harness.castFromHand(player1, new FlaringPain(), "{1}{R}");
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player2, "RED");

        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }
}

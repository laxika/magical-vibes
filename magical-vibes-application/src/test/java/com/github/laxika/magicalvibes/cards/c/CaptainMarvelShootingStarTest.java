package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UltimateNullification;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainMarvelShootingStar.class, GrizzlyBears.class, UltimateNullification.class})
class CaptainMarvelShootingStarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one creature and both life-gain abilities use its power")
    void etbExilesCreatureAndBothAbilitiesUseItsPower() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setPowerModifier(3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CaptainMarvelShootingStar()));
        addCaptainMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Attack trigger exiles a chosen creature and gains life")
    void attackTriggerExilesCreatureAndGainsLife() {
        addCreatureReady(player1, new CaptainMarvelShootingStar());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Exiling your own creature gains its power twice")
    void etbCanExileFriendlyCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CaptainMarvelShootingStar()));
        addCaptainMana();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Captain Marvel can exile herself without triggering her other ability")
    void attackCanExileSelfWithoutExtraLifeGain() {
        Permanent captain = addCreatureReady(player1, new CaptainMarvelShootingStar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, captain.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Captain Marvel, Shooting Star");
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The attack ability may choose zero targets")
    void attackCanChooseNoTargets() {
        addCreatureReady(player1, new CaptainMarvelShootingStar());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Captain Marvel, Shooting Star");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Captain Marvel sees other creatures exiled simultaneously with her")
    void gainsLifeForCreatureExiledSimultaneouslyWithSelf() {
        addCreatureReady(player1, new CaptainMarvelShootingStar());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        otherCreature.setPowerModifier(3);
        Permanent sacrifice = addCreatureReady(player2, new CaptainMarvelShootingStar());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new UltimateNullification()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        harness.castSorceryWithSacrifice(player2, 0, sacrifice.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Captain Marvel, Shooting Star");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    private void addCaptainMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}

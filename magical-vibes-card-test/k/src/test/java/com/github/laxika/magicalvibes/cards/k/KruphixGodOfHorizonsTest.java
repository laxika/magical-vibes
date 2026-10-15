package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.OmnathLocusOfMana;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KruphixGodOfHorizons.class, GrizzlyBears.class, LlanowarElves.class,
        OmnathLocusOfMana.class, TurnToFrog.class})
class KruphixGodOfHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Kruphix is an enchantment below seven devotion to green and blue")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(4);

        assertThat(gqs.isCreature(gd, kruphix)).isFalse();
        assertThat(gqs.isEnchantment(gd, kruphix)).isTrue();
    }

    @Test
    @DisplayName("Kruphix becomes a creature at seven devotion to green and blue")
    void becomesCreatureAtDevotionThreshold() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(5);

        assertThat(gqs.isCreature(gd, kruphix)).isTrue();
    }

    @Test
    @DisplayName("The controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        addKruphix();
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("The controller's mana becomes colorless instead of draining")
    void controllersManaBecomesColorlessInsteadOfDraining() {
        addKruphix();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Kruphix stops being a creature when its controller's devotion falls below seven")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(5);
        assertThat(gqs.isCreature(gd, kruphix)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.isCreature(gd, kruphix)).isFalse();
    }

    @Test
    @DisplayName("Opposing permanents do not contribute to Kruphix's devotion")
    void opponentsPermanentsDoNotContributeDevotion() {
        Permanent kruphix = addKruphix();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new LlanowarElves());
        }

        assertThat(gqs.isCreature(gd, kruphix)).isFalse();
    }

    @Test
    @DisplayName("Stored colorless mana survives successive boundaries but drains after Kruphix leaves")
    void storedManaPersistsOnlyWhileKruphixIsPresent() {
        Permanent kruphix = addKruphix();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        gs.advanceStep(gd);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(kruphix);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Mana protected by Omnath remains green while Kruphix converts other mana")
    void manaThatWouldNotBeLostDoesNotBecomeColorless() {
        addKruphix();
        harness.addToBattlefield(player1, new OmnathLocusOfMana());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kruphix does not retain mana while Turn to Frog removes its abilities")
    void manaDrainsWhenKruphixLosesItsAbilities() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, kruphix.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
    @Test
    @DisplayName("Kruphix does not remove the opponent's maximum hand size")
    void opponentStillDiscardsAtCleanup() {
        addKruphix();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        gs.advanceStep(gd);

        assertThat(gd.cleanupDiscardPending).isTrue();
    }

    @Test
    @DisplayName("Kruphix's controller must discard if Kruphix has lost its abilities")
    void controllerDiscardsWhenKruphixLosesAbilities() {
        Permanent kruphix = addKruphix();
        addGreenPermanents(5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, kruphix.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.cleanupDiscardPending).isTrue();
    }
    private Permanent addKruphix() {
        return harness.addToBattlefieldAndReturn(player1, new KruphixGodOfHorizons());
    }

    private void addGreenPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }
    }
}

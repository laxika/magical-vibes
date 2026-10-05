package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.k.KorSkyfisher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalakirBloodwitch.class, VampireNighthawk.class, IntoTheRoil.class,
        KrakenHatchling.class, KorSkyfisher.class})
class MalakirBloodwitchTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses life equal to your Vampires and you gain that much")
    void etbDrainsForVampiresYouControl() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new VampireNighthawk());
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Counts itself but excludes opposing Vampires and your non-Vampires")
    void countsOnlyVampiresYouControlIncludingItself() {
        harness.addToBattlefield(player2, new VampireNighthawk());
        harness.addToBattlefield(player1, new KrakenHatchling());
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Counts Vampires at resolution after another Vampire leaves")
    void countsVampiresAtResolution() {
        harness.addToBattlefield(player1, new VampireNighthawk());
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Vampire Nighthawk"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vampire Nighthawk");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The trigger survives its source leaving and counts only remaining Vampires")
    void triggerResolvesAfterBloodwitchLeaves() {
        harness.addToBattlefield(player1, new VampireNighthawk());
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Malakir Bloodwitch"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Malakir Bloodwitch");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("No life is lost or gained when no Vampires remain at resolution")
    void noDrainWhenNoVampiresRemain() {
        harness.setHand(player1, List.of(new MalakirBloodwitch()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Malakir Bloodwitch"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Malakir Bloodwitch");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("White flying creatures cannot block Malakir Bloodwitch")
    void whiteFlyerCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new MalakirBloodwitch());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KorSkyfisher());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection prevents combat damage from white creatures")
    void preventsWhiteCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new KorSkyfisher());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MalakirBloodwitch());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Malakir Bloodwitch");
        harness.assertInGraveyard(player1, "Kor Skyfisher");
    }

    @Test
    @DisplayName("A ground creature without reach cannot block Malakir Bloodwitch")
    void groundCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new MalakirBloodwitch());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KrakenHatchling());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}

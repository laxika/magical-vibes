package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FontOfFortunes;
import com.github.laxika.magicalvibes.cards.f.FontOfVigor;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KiorasDismissal.class, GloriousAnthem.class, AngelicChorus.class, GrizzlyBears.class,
        FontOfFortunes.class, FontOfVigor.class})
class KiorasDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns each target enchantment to its owner's hand")
    void returnsTargetEnchantmentsToTheirOwnersHands() {
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(ownEnchantment.getId(), opponentEnchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInHand(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Strive requires one additional blue mana per additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent secondEnchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstEnchantment.getId(), secondEnchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target only enchantments")
    void cannotTargetNonEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    void returnsSingleTargetForOneBlueMana() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FontOfFortunes());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Font of Fortunes");
        harness.assertInHand(player2, "Font of Fortunes");
        harness.assertInGraveyard(player1, "Kiora's Dismissal");
    }

    @Test
    void returnsThreeTargetsForThreeBlueMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FontOfFortunes());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FontOfVigor());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new FontOfFortunes());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Font of Fortunes");
        harness.assertNotOnBattlefield(player1, "Font of Vigor");
        harness.assertNotOnBattlefield(player2, "Font of Fortunes");
        harness.assertInHand(player1, "Font of Fortunes");
        harness.assertInHand(player1, "Font of Vigor");
        harness.assertInHand(player2, "Font of Fortunes");
    }

    @Test
    void cannotChooseSameEnchantmentTwice() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FontOfFortunes());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(enchantment.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsRemainingTargetWhenOtherTargetIsSacrificed() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new FontOfVigor());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new FontOfFortunes());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(sacrificed.getId(), remaining.getId()));
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Font of Vigor");
        harness.assertNotInHand(player2, "Font of Vigor");
        harness.assertNotOnBattlefield(player2, "Font of Fortunes");
        harness.assertInHand(player2, "Font of Fortunes");
    }

    @Test
    void canReturnMoreThanNinetyNineEnchantments() {
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new FontOfFortunes()).getId());
        }
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 100);

        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Font of Fortunes");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card instanceof FontOfFortunes)
                .hasSize(100);
    }
}

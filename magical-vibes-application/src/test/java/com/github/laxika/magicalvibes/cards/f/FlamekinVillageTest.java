package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FlamekinVillage.class, GrizzlyBears.class, IvyElemental.class})
class FlamekinVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when no Elemental card is revealed")
    void entersTappedWithoutElemental() {
        harness.setHand(player1, List.of(new FlamekinVillage(), new GrizzlyBears()));
        castVillageFromHand();

        assertThat(findVillage().isTapped()).isTrue();
    }

    @Test
    @DisplayName("May reveal an Elemental card to enter untapped")
    void entersUntappedWhenRevealingElemental() {
        harness.setHand(player1, List.of(new FlamekinVillage(), new IvyElemental()));
        castVillageFromHand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findVillage().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal an Elemental makes it enter tapped")
    void entersTappedWhenDecliningReveal() {
        harness.setHand(player1, List.of(new FlamekinVillage(), new IvyElemental()));
        castVillageFromHand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findVillage().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tapsForRedMana() {
        harness.addToBattlefield(player1, new FlamekinVillage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Red activation grants target creature haste until end of turn")
    void grantsHasteUntilEndOfTurn() {
        harness.addToBattlefield(player1, new FlamekinVillage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste activation cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FlamekinVillage());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new FlamekinVillage());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castVillageFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }

    private Permanent findVillage() {
        return findPermanent(player1, "Flamekin Village");
    }
}

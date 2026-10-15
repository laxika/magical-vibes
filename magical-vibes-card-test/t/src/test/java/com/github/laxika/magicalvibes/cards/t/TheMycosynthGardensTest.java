package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMycosynthGardens.class, RodOfRuin.class, PropheticPrism.class})
class TheMycosynthGardensTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying one and tapping adds one mana of a chosen color")
    void paysOneAndTapsForAnyColor() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Becomes a copy of a controlled nontoken artifact with mana value X")
    void becomesCopyOfTargetArtifactWithManaValueX() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, 4, target.getId());
        harness.passBothPriorities();

        Permanent gardens = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gardens.getCard().getName()).isEqualTo("Rod of Ruin");
        assertThat(gardens.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an artifact with a different mana value")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact controlled by an opponent")
    void cannotTargetOpponentArtifact() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 4, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTokenArtifact() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        PropheticPrism token = new PropheticPrism();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, token);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonartifactEvenWithMatchingManaValue() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheMycosynthGardens());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyRemainsTappedDoesNotTriggerEnteringAndCanUseCopiedManaAbility() {
        Permanent gardens = harness.addToBattlefieldAndReturn(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, 2, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gardens.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);

        gardens.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void doesNotCopyTargetThatLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, 2, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Mycosynth Gardens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyTargetThatChangesControllerBeforeResolution() {
        harness.addToBattlefield(player1, new TheMycosynthGardens());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, 2, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Mycosynth Gardens");
        assertThat(gd.stack).isEmpty();
    }
}

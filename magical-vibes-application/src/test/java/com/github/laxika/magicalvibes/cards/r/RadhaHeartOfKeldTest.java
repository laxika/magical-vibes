package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RadhaHeartOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn only")
    void firstStrikeDuringControllerTurn() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeartOfKeld());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, radha, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, radha, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can play a land from the top of its controller's library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not allow playing a land from the top without Radha")
    void cannotPlayLandFromLibraryTopWithoutRadha() {
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }

    @Test
    @DisplayName("The activated ability gets +X/+X for the number of lands controlled")
    void activatedAbilityBoostsByLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeartOfKeld());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, radha)).isEqualTo(5);
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void activatedAbilityBoostWearsOff() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeartOfKeld());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot cast a nonland card from the top with Radha alone")
    void cannotCastNonlandFromLibraryTop() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not castable");
    }
}

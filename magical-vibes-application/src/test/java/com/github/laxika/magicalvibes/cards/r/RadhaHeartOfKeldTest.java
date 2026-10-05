package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RadhaHeartOfKeld.class, Forest.class, GrizzlyBears.class})
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
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, radha)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, radha)).isEqualTo(3);
    }

    @Test
    void topCardIsVisibleOnlyToControllerDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains(top.getId().toString()));
    }

    @Test
    void cannotPlaySecondLandFromLibraryTop() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayLandFromLibraryDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPlayLandFromLibraryWhileAbilityIsOnStack() {
        harness.addToBattlefield(player1, new RadhaHeartOfKeld());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void boostCountsLandsAtResolutionAndRemainsFixedAfterward() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeartOfKeld());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, radha)).isEqualTo(5);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, radha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, radha)).isEqualTo(5);
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

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamutTheTested.class, GrizzlyBears.class, Forest.class, Plains.class,
        SamutVoiceOfDissent.class, FrilledSandwalla.class, NicolBolasGodPharaoh.class})
class SamutTheTestedTest extends BaseCardTest {

    // ===== +1: up to one creature gains double strike =====

    @Test
    @DisplayName("+1 grants double strike to target creature and raises loyalty")
    void plusOneGrantsDoubleStrike() {
        Permanent samut = addReadySamut(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("+1 may choose no target and still raises loyalty")
    void plusOneWithNoTarget() {
        Permanent samut = addReadySamut(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 double strike wears off at end of turn")
    void plusOneWearsOffAtEndOfTurn() {
        addReadySamut(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("+1 cannot target a noncreature permanent")
    void plusOneCannotTargetNoncreature() {
        addReadySamut(player1, 4);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== −2: 2 damage divided among one or two targets =====

    @Test
    @DisplayName("-2 deals 2 damage to a single creature")
    void minusTwoDealsAllDamageToOneCreature() {
        Permanent samut = addReadySamut(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithDamageAssignments(player1, 0, 1, null, Map.of(bears.getId(), 2));
        harness.passBothPriorities();

        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("-2 divides damage among creature and player")
    void minusTwoDividesAmongCreatureAndPlayer() {
        Permanent samut = addReadySamut(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 1, null, Map.of(bears.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    // ===== −7: search for up to two creatures/planeswalkers =====

    @Test
    @DisplayName("-7 offers up to two creature or planeswalker cards to the battlefield")
    void minusSevenOffersCreaturesAndPlaneswalkers() {
        Permanent samut = addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Plains(), new SamutVoiceOfDissent()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(samut.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Samut, Voice of Dissent")
                .doesNotContain("Plains");
    }

    @Test
    @DisplayName("-7 puts chosen cards onto the battlefield")
    void minusSevenPutsOntoBattlefield() {
        addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new SamutVoiceOfDissent(), new Plains()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.CREATURE))
                .extracting(p -> p.getCard().getName())
                .contains("Grizzly Bears", "Samut, Voice of Dissent");
    }

    @Test
    @DisplayName("Cannot activate -7 with insufficient loyalty")
    void cannotActivateMinusSevenWithLowLoyalty() {
        addReadySamut(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    void plusOneCanTargetOpponentsCreature() {
        addReadySamut(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void minusTwoCanDamagePlaneswalker() {
        addReadySamut(player1, 4);
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbilityWithDamageAssignments(player1, 0, 1, null, Map.of(bolas.getId(), 2));
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusTwoResolvesAfterPayingLastLoyalty() {
        addReadySamut(player1, 2);
        harness.activateAbilityWithDamageAssignments(player1, 0, 1, null, Map.of(player2.getId(), 2));

        harness.assertNotOnBattlefield(player1, "Samut, the Tested");
        harness.assertInGraveyard(player1, "Samut, the Tested");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void minusTwoDoesNotRedistributeDamageFromMissingTarget() {
        addReadySamut(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());
        harness.activateAbilityWithDamageAssignments(player1, 0, 1, null,
                Map.of(creature.getId(), 1, player2.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void minusTwoRejectsNoTargets() {
        addReadySamut(player1, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 1, null, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoRejectsZeroDamageTarget() {
        addReadySamut(player1, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 1, null, Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoRejectsAssignmentsAboveTwoDamage() {
        addReadySamut(player1, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 1, null, Map.of(player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSevenPutsCreatureAndPlaneswalkerOntoBattlefieldTogether() {
        addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of(new FrilledSandwalla(), new NicolBolasGodPharaoh(), new Plains()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Frilled Sandwalla");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Frilled Sandwalla");
        harness.assertOnBattlefield(player1, "Nicol Bolas, God-Pharaoh");
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void minusSevenMayFindNoCards() {
        addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of(new FrilledSandwalla(), new NicolBolasGodPharaoh()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void minusSevenMayStopAfterOneCard() {
        addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of(new FrilledSandwalla(), new NicolBolasGodPharaoh()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Frilled Sandwalla");
        harness.assertNotOnBattlefield(player1, "Nicol Bolas, God-Pharaoh");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Nicol Bolas, God-Pharaoh");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void minusSevenWithEmptyLibraryFinishes() {
        addReadySamut(player1, 7);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySamut(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SamutTheTested());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}

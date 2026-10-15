package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExcaliburSwordOfEden;
import com.github.laxika.magicalvibes.cards.e.EzioBrashNovice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hookblade;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.UrzasSylex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemplarKnight.class, UrzasSylex.class, GrizzlyBears.class, Spellbook.class,
        ExcaliburSwordOfEden.class, EzioBrashNovice.class, Hookblade.class})
class TemplarKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Taps five attacking Templar Knights to put a legendary artifact from the library onto the battlefield")
    void searchesForLegendaryArtifact() {
        Permanent source = addTemplar(false);
        List<Permanent> attackingTemplars = addTemplars(5, true);
        UrzasSylex sylex = new UrzasSylex();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Spellbook(), sylex));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);

        assertThat(source.isTapped()).isFalse();
        assertThat(attackingTemplars).allMatch(Permanent::isTapped);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sylex);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == sylex);
    }

    @Test
    @DisplayName("Requires five untapped attacking creatures named Templar Knight")
    void requiresFiveMatchingAttackers() {
        addTemplar(false);
        addTemplars(4, true);
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        attackingBears.setAttacking(true);
        Permanent nonattackingTemplar = addTemplar(false);
        nonattackingTemplar.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("An attacking source can be one of the five creatures tapped")
    void attackingSourceCanPayTheCost() {
        List<Permanent> attackers = addTemplars(5, true);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);

        assertThat(attackers).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick source can activate using five other attacking Knights")
    void tappedSummoningSickSourceCanActivate() {
        Permanent source = addTemplar(false);
        source.tap();
        source.setSummoningSick(true);
        List<Permanent> attackers = addTemplars(5, true);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);

        assertThat(attackers).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped attacking Knight cannot count toward the five untapped attackers")
    void tappedAttackerCannotPayTheCost() {
        addTemplar(false);
        List<Permanent> attackers = addTemplars(5, true);
        attackers.getFirst().tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");

        assertThat(attackers.subList(1, 5)).noneMatch(Permanent::isTapped);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's attacking Knight cannot pay the activation cost")
    void opponentsKnightCannotPayTheCost() {
        addTemplar(false);
        List<Permanent> attackers = addTemplars(4, true);
        Permanent opponentsKnight = addCreatureReady(player2, new TemplarKnight());
        opponentsKnight.setAttacking(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");

        assertThat(attackers).noneMatch(Permanent::isTapped);
        assertThat(opponentsKnight.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The restricted search may fail to find even with a legendary artifact present")
    void canDeclineToFindLegendaryArtifact() {
        addTemplar(false);
        List<Permanent> attackers = addTemplars(5, true);
        UrzasSylex sylex = new UrzasSylex();
        harness.setLibrary(player1, List.of(sylex));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(attackers).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sylex);
        harness.assertNotOnBattlefield(player1, "Urza's Sylex");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A library containing no legendary artifacts completes the search without a choice")
    void noMatchingCardCompletesSearch() {
        addTemplar(false);
        addTemplars(5, true);
        TemplarKnight libraryCard = new TemplarKnight();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The search requires both legendary and artifact and does not pay the found card's mana cost")
    void searchExcludesLegendaryNonartifacts() {
        addTemplar(false);
        addTemplars(5, true);
        ExcaliburSwordOfEden excalibur = new ExcaliburSwordOfEden();
        harness.setLibrary(player1, List.of(new EzioBrashNovice(), new Hookblade(), excalibur));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(excalibur);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == excalibur && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(excalibur).hasSize(2);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("With six matching attackers the controller chooses exactly five to tap")
    void choosesFiveOfSixAttackers() {
        addTemplar(false);
        List<Permanent> attackers = addTemplars(6, true);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null);
        for (Permanent attacker : attackers.subList(1, 6)) {
            harness.handlePermanentChosen(player1, attacker.getId());
        }

        assertThat(attackers.getFirst().isTapped()).isFalse();
        assertThat(attackers.subList(1, 6)).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTemplar(boolean attacking) {
        Permanent permanent = addCreatureReady(player1, new TemplarKnight());
        permanent.setAttacking(attacking);
        return permanent;
    }

    private List<Permanent> addTemplars(int count, boolean attacking) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> addTemplar(attacking))
                .toList();
    }
}

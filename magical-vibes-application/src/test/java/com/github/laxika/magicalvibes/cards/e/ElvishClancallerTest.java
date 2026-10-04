package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({ElvishClancaller.class, LlanowarElves.class, GrizzlyBears.class})
class ElvishClancallerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherElvesYouControl() {
        harness.addToBattlefield(player1, new ElvishClancaller());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elvish Clancaller does not buff itself, non-Elves, or opposing Elves")
    void onlyBuffsOtherOwnElves() {
        Permanent clancaller = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, clancaller)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, clancaller)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability searches for an Elvish Clancaller and puts it onto the battlefield")
    void searchesNamedCardToBattlefield() {
        Permanent clancaller = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        clancaller.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new ElvishClancaller()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).allMatch(card -> card.getName().equals("Elvish Clancaller"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.getCard().getName().equals("Elvish Clancaller"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Multiple Clancallers boost each other and their bonuses stack")
    void multipleClancallersStackTheirBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());

        for (Permanent clancaller : List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, clancaller)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, clancaller)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Activation pays four generic and two green mana and taps the source")
    void activationPaysManaAndTapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new ElvishClancaller()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent found = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(found.isTapped()).isFalse();
        assertThat(found.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, found)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, found)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvishClancaller());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability cannot be activated while already tapped")
    void cannotActivateWhileTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        source.tap();
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Six mana with only one green cannot pay the activation cost")
    void activationRequiresTwoGreenMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A restricted search can fail to find even when a Clancaller is available")
    void canDeclineToFindMatchingCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        ElvishClancaller matching = new ElvishClancaller();
        harness.setLibrary(player1, List.of(matching));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matching);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library resolves without a choice")
    void emptyLibraryResolvesWithoutChoice() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one Clancaller is found when several are in the library")
    void findsExactlyOneMatchingCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        ElvishClancaller first = new ElvishClancaller();
        ElvishClancaller second = new ElvishClancaller();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getCard()).isSameAs(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with no matching card resolves without moving another card")
    void noMatchingCardResolvesWithoutChoice() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search resolves after its source leaves the battlefield")
    void searchResolvesWithoutSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ElvishClancaller());
        source.setSummoningSick(false);
        ElvishClancaller matching = new ElvishClancaller();
        harness.setLibrary(player1, List.of(matching));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent found = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(found.getCard()).isSameAs(matching);
        assertThat(gqs.getEffectivePower(gd, found)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, found)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

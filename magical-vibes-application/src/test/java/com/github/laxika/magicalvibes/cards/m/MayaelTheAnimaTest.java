package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.b.BranchingBolt;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MayaelTheAnima.class, Mosstodon.class, CylianElf.class, BranchingBolt.class,
        Plains.class, WoollyThoctar.class})
class MayaelTheAnimaTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only creature cards with power 5 or greater")
    void offersOnlyHighPowerCreatures() {
        setupMayaelAndActivate(List.of(
                new Mosstodon(),
                new CylianElf(),
                new CylianElf(),
                new BranchingBolt(),
                new Plains()
        ));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst()).isInstanceOf(Mosstodon.class);
    }

    @Test
    @DisplayName("Choosing a qualifying creature puts it onto the battlefield and reorders the rest")
    void choosingPutsCreatureOnBattlefield() {
        setupMayaelAndActivate(List.of(
                new Mosstodon(), new CylianElf(), new BranchingBolt(), new Plains(), new Plains()
        ));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mosstodon");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("May decline to put a creature onto the battlefield")
    void mayDeclineToPutCreature() {
        setupMayaelAndActivate(List.of(
                new Mosstodon(), new CylianElf(), new BranchingBolt(), new Plains(), new Plains()
        ));

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("No qualifying creatures sends all five cards to the bottom")
    void noQualifyingCreaturesReordersAll() {
        setupMayaelAndActivate(List.of(
                new CylianElf(), new CylianElf(), new CylianElf(), new BranchingBolt(), new Plains()
        ));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    /**
     * Puts a ready Mayael on player1's battlefield, stacks the given cards on top of the library,
     * then pays for and resolves the {@code {3}{R}{G}{W}, {T}} ability.
     */
    private void setupMayaelAndActivate(List<Card> topCards) {
        addCreatureReady(player1, new MayaelTheAnima());

        harness.setLibrary(player1, topCards);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    void choosesOnlyOneCreatureFromTheTopFiveAndOrdersTheRestBelowUntouchedCards() {
        Card first = new Mosstodon();
        Card chosen = new WoollyThoctar();
        Card elf = new CylianElf();
        Card bolt = new BranchingBolt();
        Card land = new Plains();
        Card untouched = new Mosstodon();
        setupMayaelAndActivate(List.of(first, chosen, elf, bolt, land, untouched));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(first, chosen);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2).extracting(permanent -> permanent.getCard()).contains(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isSummoningSick()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, land, elf, first, bolt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningOrdersAllFiveCardsBelowTheUntouchedLibrary() {
        List<Card> lookedAt = List.of(new Mosstodon(), new CylianElf(), new BranchingBolt(),
                new Plains(), new WoollyThoctar());
        Card untouched = new Plains();
        setupMayaelAndActivate(List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));

        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched,
                lookedAt.get(4), lookedAt.get(3), lookedAt.get(2), lookedAt.get(1), lookedAt.get(0));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCreatureStillAllowsChoosingTheBottomOrder() {
        Card elf = new CylianElf();
        Card bolt = new BranchingBolt();
        Card land = new Plains();
        setupMayaelAndActivate(List.of(elf, bolt, land));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, elf, bolt);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryCanPutACreatureOntoTheBattlefieldAndReturnTheOnlyRemainingCard() {
        Card chosen = new Mosstodon();
        Card remaining = new Plains();
        setupMayaelAndActivate(List.of(chosen, remaining));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mosstodon");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyCardCanBeChosenWithoutAReorderPrompt() {
        setupMayaelAndActivate(List.of(new WoollyThoctar()));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Woolly Thoctar");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        setupMayaelAndActivate(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

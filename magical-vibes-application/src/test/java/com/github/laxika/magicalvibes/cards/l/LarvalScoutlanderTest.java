package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GalacticWayfarer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LarvalScoutlander.class, Forest.class, Mountain.class, Island.class,
        GalacticWayfarer.class, GrizzlyBears.class})
class LarvalScoutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("Entering may sacrifice a land to search for up to two tapped basic lands")
    void enteringSacrificesLandAndSearchesForBasicLands() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        Island island = new Island();
        setLibrary(forest, mountain, island);

        castLarvalScoutlander();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificedLand.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);

        chooseLibraryCard(0);
        chooseLibraryCard(0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedLand.getCard());
        assertThat(findPermanent(forest)).matches(Permanent::isTapped);
        assertThat(findPermanent(mountain)).matches(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the entry ability does not sacrifice or search")
    void decliningEntryAbilityDoesNothing() {
        Forest land = new Forest();
        harness.addToBattlefield(player1, land);
        Forest forest = new Forest();
        setLibrary(forest);

        castLarvalScoutlander();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("A Lander can be sacrificed for the entry ability")
    void enteringCanSacrificeLander() {
        createLander();
        Forest forest = new Forest();
        setLibrary(forest);

        castLarvalScoutlander();
        harness.handleMayAbilityChosen(player1, true);
        Permanent lander = findPermanents(player1, "Lander").getFirst();
        harness.handlePermanentChosen(player1, lander.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        chooseLibraryCard(0);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard() == forest && permanent.isTapped());
    }

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent scoutlander = harness.addToBattlefieldAndReturn(player1, new LarvalScoutlander());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(scoutlander), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(scoutlander.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At seven charge counters, Larval Scoutlander becomes a flying artifact creature")
    void sevenCountersAnimateAndGrantFlying() {
        Permanent scoutlander = harness.addToBattlefieldAndReturn(player1, new LarvalScoutlander());

        scoutlander.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, scoutlander)).isFalse();
        assertThat(gqs.hasKeyword(gd, scoutlander, Keyword.FLYING)).isFalse();

        scoutlander.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, scoutlander)).isTrue();
        assertThat(gqs.hasKeyword(gd, scoutlander, Keyword.FLYING)).isTrue();

        scoutlander.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, scoutlander)).isFalse();
        assertThat(gqs.hasKeyword(gd, scoutlander, Keyword.FLYING)).isFalse();
    }

    private void castLarvalScoutlander() {
        harness.setHand(player1, List.of(new LarvalScoutlander()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void createLander() {
        harness.setHand(player1, List.of(new GalacticWayfarer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseLibraryCard(int index) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}

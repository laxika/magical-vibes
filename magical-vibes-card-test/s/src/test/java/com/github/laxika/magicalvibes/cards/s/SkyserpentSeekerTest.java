package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Skyserpent Seeker")
@CardUsed({SkyserpentSeeker.class, Forest.class, Island.class, Shock.class, GrizzlyBears.class})
class SkyserpentSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts the revealed lands onto the battlefield tapped and grows Skyserpent Seeker")
    void exhaustPutsLandsTappedAndAddsCounter() {
        Permanent seeker = addSeeker();
        Shock shock = new Shock();
        Forest forest = new Forest();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, island, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard().getId().equals(island.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("An exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addSeeker();
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust stops at the second land and bottoms only the revealed nonlands")
    void stopsAtSecondLand() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SkyserpentSeeker());
        seeker.setSummoningSick(true);
        seeker.tap();
        Shock first = new Shock();
        GrizzlyBears second = new GrizzlyBears();
        Forest forest = new Forest();
        Island island = new Island();
        SkyserpentSeeker unrevealed = new SkyserpentSeeker();
        Forest unrevealedLand = new Forest();
        harness.setLibrary(player1, List.of(first, forest, second, island, unrevealed, unrevealedLand));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(seeker.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).extracting(Card::getId)
                .containsExactly(unrevealed.getId(), unrevealedLand.getId());
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4)).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard().getId().equals(island.getId()) && permanent.isTapped())
                .noneMatch(permanent -> permanent.getCard().getId().equals(unrevealedLand.getId()));
    }

    @Test
    @DisplayName("Exhaust puts the only land onto the battlefield and still adds a counter")
    void libraryContainsOnlyOneLand() {
        Permanent seeker = addSeeker();
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("Exhaust with no lands keeps every card in the library and still adds a counter")
    void libraryContainsNoLands() {
        Permanent seeker = addSeeker();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seeker);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("Exhaust with an empty library still adds a counter")
    void emptyLibraryStillAddsCounter() {
        Permanent seeker = addSeeker();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Removing the seeker in response does not prevent the lands from entering")
    void sourceRemovedBeforeResolution() {
        Permanent seeker = addSeeker();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player2, 0, seeker.getId());
        harness.assertInGraveyard(player1, "Skyserpent Seeker");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard().getId().equals(island.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Skyserpent Seeker");
    }

    @Test
    @DisplayName("Exhaust cannot be activated a second time while the first activation is on the stack")
    void cannotActivateTwiceBeforeResolution() {
        addSeeker();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
    }

    private Permanent addSeeker() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SkyserpentSeeker());
        seeker.setSummoningSick(false);
        return seeker;
    }
}

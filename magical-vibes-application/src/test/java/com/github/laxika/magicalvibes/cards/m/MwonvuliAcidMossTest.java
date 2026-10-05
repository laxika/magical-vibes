package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({MwonvuliAcidMoss.class, Forest.class, Island.class, AshcoatBear.class})
class MwonvuliAcidMossTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target land and puts a Forest onto the battlefield tapped")
    void destroysTargetLandAndSearchesForForest() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Island(), new AshcoatBear()));
        castAndResolveSpell(targetLand);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetLand.getId()));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find a Forest")
    void canFailToFindForest() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setLibrary(player1, List.of(new Island(), new AshcoatBear()));
        castAndResolveSpell(targetLand);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetLand.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MwonvuliAcidMoss()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can fail to find even when a Forest is available")
    void canDeclineAvailableForest() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Island()));
        castAndResolveSpell(targetLand);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest).hasSize(2);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can destroy your own land and search your own library")
    void canTargetOwnLand() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castAndResolveSpell(targetLand);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still destroys the land and shuffles an empty library")
    void resolvesWithEmptyLibrary() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setLibrary(player1, List.of());
        castAndResolveSpell(targetLand);

        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not search or shuffle when the only target leaves before resolution")
    void doesNotSearchWhenTargetIsGone() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new MwonvuliAcidMoss()));
        addMana();
        harness.castSorcery(player1, 0, targetLand.getId());

        gd.playerBattlefields.get(player2.getId()).remove(targetLand);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mwonvuli Acid-Moss");
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveSpell(Permanent targetLand) {
        harness.setHand(player1, List.of(new MwonvuliAcidMoss()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetLand.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

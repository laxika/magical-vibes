package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisorientingChoice.class, Forest.class, GloriousAnthem.class, Plains.class, SolRing.class})
class DisorientingChoiceTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent may keep a chosen permanent, then the caster searches for a tapped land")
    void keepsPermanentAndSearchesForSurvivors() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(anthem.getId()));
        harness.handleListChoice(player2, "Keep it");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(anthem);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opponent may exile a chosen permanent, preventing the follow-up search when none survive")
    void exilesPermanentAndSkipsSearchWhenNoneSurvive() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(ring.getId()));
        harness.handleListChoice(player2, "Exile it");

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent.getId().equals(ring.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only opponent-controlled artifacts and enchantments can be chosen")
    void rejectsInvalidTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster may choose no targets even when an opponent controls a legal target")
    void mayChooseNoTargetsWithLegalTargetAvailable() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new Forest()));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ring);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Disorienting Choice");
    }

    @Test
    @DisplayName("The spell resolves without choices or a search when no targets are chosen")
    void resolvesWithoutTargets() {
        harness.setLibrary(player1, List.of(new Forest()));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Disorienting Choice");
    }

    @Test
    @DisplayName("The caster cannot target their own artifact")
    void rejectsCasterControlledArtifact() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ring.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At most one permanent may be targeted for each opponent")
    void rejectsTwoTargetsControlledBySameOpponent() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ring.getId(), anthem.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster may find zero lands even when a land is available")
    void mayDeclineToFindLand() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(ring.getId()));
        harness.handleListChoice(player2, "Keep it");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ring);
    }

    @Test
    @DisplayName("A surviving target does not allow searching for a nonland card")
    void searchWithNoLandsFinishesWithoutPuttingNonlandOntoBattlefield() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new SolRing()));

        cast(List.of(ring.getId()));
        harness.handleListChoice(player2, "Keep it");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponents decide in turn order, then every permanent to exile is exiled at once (ruling)")
    void opponentsDecideInTurnOrderThenExileTogether() {
        Player player3 = addThirdPlayer();
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent anthem = harness.addToBattlefieldAndReturn(player3, new GloriousAnthem());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(anthem.getId(), ring.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Exile it");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ring);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.handleListChoice(player3, "Exile it");

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ring);
        assertThat(gd.playerBattlefields.get(player3.getId())).doesNotContain(anthem);
        assertThat(gd.findExiledCard(ring.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(anthem.getCard().getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Player addThirdPlayer() {
        Player player3 = new Player(UUID.randomUUID(), "Third opponent");
        gd.playerIds.add(player3.getId());
        gd.orderedPlayerIds.add(player3.getId());
        gd.playerNames.add(player3.getUsername());
        gd.playerIdToName.put(player3.getId(), player3.getUsername());
        gd.playerDecks.put(player3.getId(), new ArrayList<>());
        gd.playerHands.put(player3.getId(), new ArrayList<>());
        gd.playerGraveyards.put(player3.getId(), new ArrayList<>());
        gd.playerBattlefields.put(player3.getId(), new ArrayList<>());
        gd.playerManaPools.put(player3.getId(), new ManaPool());
        gd.playerLifeTotals.put(player3.getId(), 20);
        return player3;
    }

    private void cast(List<UUID> targetIds) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, targetIds);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DisorientingChoice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

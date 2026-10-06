package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FlowstoneThopter;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RathiIntimidator.class, SpinelessThug.class, Mossdog.class, RathiFiend.class,
        FlowstoneThopter.class})
class RathiIntimidatorTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostTwo() {
        addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player1, List.of(new SpinelessThug(), new Mossdog(), new RathiFiend()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Spineless Thug");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Spineless Thug");
        harness.assertNotOnBattlefield(player1, "Mossdog");
        harness.assertNotOnBattlefield(player1, "Rathi Fiend");
        assertThat(findPermanent(player1, "Spineless Thug").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Spineless Thug").isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mossdog", "Rathi Fiend");
    }

    @Test
    @DisplayName("Fear prevents nonblack, nonartifact creatures from blocking")
    void fearPreventsNonBlackNonArtifactBlockers() {
        addCreatureReady(player1, new RathiIntimidator());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block")
    void fearAllowsBlackAndArtifactBlockers() {
        addCreatureReady(player1, new RathiIntimidator());
        addCreatureReady(player2, new RathiFiend());
        addCreatureReady(player2, new FlowstoneThopter());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The search resolves without a choice when no card matches")
    void resolvesWithoutChoiceWhenNoMercenaryMatches() {
        addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Mossdog(), new RathiFiend()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mossdog", "Rathi Fiend");
    }

    @Test
    void mayFailToFindEvenWhenAnEligibleMercenaryExists() {
        addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new SpinelessThug()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Spineless Thug");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Spineless Thug");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activationTapsSourceAndCannotBeRepeatedWhileTapped() {
        Permanent source = addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new SpinelessThug()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Spineless Thug");
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent source = addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new RathiIntimidator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new SpinelessThug()));

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Spineless Thug");
        harness.assertInGraveyard(player1, "Rathi Intimidator");
    }
}

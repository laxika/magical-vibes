package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroesHangout.class, Forest.class, GrizzlyBears.class})
class HeroesHangoutTest extends BaseCardTest {

    @Test
    @DisplayName("Date Night exiles two cards and grants play permission only to the chosen card")
    void dateNightExilesTwoCardsAndGrantsChosenCardPermission() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        cast(0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(second.getId(), player1.getId())
                .doesNotContainKey(first.getId());
        harness.castFromExile(player1, second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Patrol Night boosts one or two target creatures and grants first strike")
    void patrolNightBoostsOneOrTwoCreaturesAndGrantsFirstStrike() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        cast(1, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(first.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(third.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Patrol Night effects wear off at cleanup")
    void patrolNightEffectsWearOffAtCleanup() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        cast(1, List.of(target.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Patrol Night rejects a noncreature target")
    void patrolNightRequiresCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HeroesHangout()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dateNightWithEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());

        cast(0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Heroes' Hangout");
    }

    @Test
    void dateNightWithOneCardStillAllowsPlayingIt() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        cast(0, List.of());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void dateNightRequiresChoosingExactlyOneOfTheExiledCards() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        cast(0, List.of());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dateNightPermissionLastsThroughNextTurnThenExpires() {
        Card chosen = new Forest();
        harness.setLibrary(player1, List.of(chosen, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        cast(0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosen.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
    }

    @Test
    void dateNightDoesNotWaiveManaCosts() {
        Card chosen = new HeroesHangout();
        harness.setLibrary(player1, List.of(chosen, new Forest()));
        cast(0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
    }

    @Test
    void patrolNightRejectsZeroTargets() {
        harness.setHand(player1, List.of(new HeroesHangout()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void patrolNightRejectsMoreThanTwoTargets() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroesHangout()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void patrolNightResolvesForRemainingTarget() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroesHangout()));
        addMana();
        harness.castModalSorcery(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    private void cast(int mode, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new HeroesHangout()));
        addMana();
        harness.castModalSorcery(player1, 0, mode, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}

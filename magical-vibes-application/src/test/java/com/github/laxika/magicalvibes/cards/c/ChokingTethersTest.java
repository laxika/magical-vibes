package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({ChokingTethers.class, GlorySeeker.class, Forest.class})
class ChokingTethersTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to four target creatures")
    void tapsFourTargetCreatures() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()));
        prepareSpell();

        harness.castInstant(player1, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Resolves with no target creatures")
    void resolvesWithNoTargetCreatures() {
        prepareSpell();

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Choking Tethers");
    }

    @Test
    @DisplayName("Cannot target more than four creatures")
    void cannotTargetMoreThanFourCreatures() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()),
                harness.addToBattlefieldAndReturn(player2, new GlorySeeker()));
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling may tap a target creature before drawing")
    void cyclingMayTapTargetCreatureAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        prepareCycling();

        cycleAndChoose(creature, true);

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Choking Tethers");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Declining the cycling tap still draws a card")
    void decliningCyclingTapStillDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        prepareCycling();

        cycleAndChoose(creature, false);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Choking Tethers");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling with no legal target still draws a card")
    void cyclingWithNoLegalTargetStillDraws() {
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Choking Tethers");
        harness.assertInHand(player1, "Glory Seeker");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ChokingTethers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareCycling() {
        harness.setHand(player1, List.of(new ChokingTethers()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void cycleAndChoose(Permanent target, boolean accept) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }
}

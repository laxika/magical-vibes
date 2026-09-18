package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BladeSliver;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoblinGoon;
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

@CardUsed({HunterSliver.class, BladeSliver.class, FugitiveWizard.class, GoblinGoon.class})
class HunterSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Hunter Sliver lets another Sliver untap a defending creature and force it to block")
    void sliversGainProvoke() {
        Permanent hunter = addCreatureReady(player1, new HunterSliver());
        Permanent attacker = addCreatureReady(player2, new BladeSliver());
        Permanent blocker = addCreatureReady(player1, new FugitiveWizard());
        blocker.tap();

        declareAttackers(player2, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(hunter.getId(), blocker.getId());

        harness.handlePermanentChosen(player2, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMustBlockIds()).containsExactly(attacker.getId());

        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining Hunter Sliver's provoke leaves the chosen creature unchanged")
    void decliningProvokeDoesNothing() {
        addCreatureReady(player1, new HunterSliver());
        addCreatureReady(player2, new BladeSliver());
        Permanent blocker = addCreatureReady(player1, new FugitiveWizard());
        blocker.tap();

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Hunter Sliver's provoke only offers creatures controlled by the defending player")
    void provokeFiltersTargets() {
        Permanent hunter = addCreatureReady(player1, new HunterSliver());
        addCreatureReady(player2, new BladeSliver());
        Permanent defendingCreature = addCreatureReady(player1, new FugitiveWizard());
        Permanent attackingPlayerCreature = addCreatureReady(player2, new FugitiveWizard());

        declareAttackers(player2, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(hunter.getId(), defendingCreature.getId())
                .doesNotContain(attackingPlayerCreature.getId());
    }

    @Test
    @DisplayName("Non-Slivers do not gain Hunter Sliver's provoke ability")
    void nonSliversDoNotGainProvoke() {
        addCreatureReady(player1, new HunterSliver());
        addCreatureReady(player2, new FugitiveWizard());
        addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Hunter Sliver's provoke does not prompt when the defending player controls no creatures")
    void provokeWithoutLegalTargetDoesNotPrompt() {
        addCreatureReady(player1, new HunterSliver());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Provoke does not require an unable creature to block")
    void provokeDoesNotRequireUnableCreatureToBlock() {
        Permanent hunter = addCreatureReady(player1, new HunterSliver());
        Permanent blocker = addCreatureReady(player2, new GoblinGoon());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        prepareDeclareBlockers(player1);
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(blocker.getMustBlockIds()).containsExactly(hunter.getId());
    }
}

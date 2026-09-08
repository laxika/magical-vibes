package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HunterSliver.class, MetallicSliver.class, GrizzlyBears.class})
class HunterSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Hunter Sliver lets another Sliver untap a defending creature and force it to block")
    void sliversGainProvoke() {
        Permanent hunter = addCreatureReady(player1, new HunterSliver());
        Permanent attacker = addCreatureReady(player2, new MetallicSliver());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
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
    }

    @Test
    @DisplayName("Declining Hunter Sliver's provoke leaves the chosen creature unchanged")
    void decliningProvokeDoesNothing() {
        addCreatureReady(player1, new HunterSliver());
        addCreatureReady(player2, new MetallicSliver());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
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
        addCreatureReady(player2, new MetallicSliver());
        Permanent defendingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackingPlayerCreature = addCreatureReady(player2, new GrizzlyBears());

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
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}

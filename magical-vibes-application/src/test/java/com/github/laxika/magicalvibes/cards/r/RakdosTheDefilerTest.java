package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosTheDefiler.class, GrizzlyBears.class, Forest.class})
class RakdosTheDefilerTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, its controller sacrifices half their non-Demon permanents, rounded up")
    void attacksSacrificeHalfNonDemonPermanents() {
        Permanent rakdos = addCreatureReady(player1, new RakdosTheDefiler());
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rakdos)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(bear1.getId(), bear2.getId(), forest.getId())
                .doesNotContain(rakdos.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bear1.getId(), forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getId))
                .containsExactly(rakdos.getId(), bear2.getId());
    }

    @Test
    @DisplayName("Combat damage makes the damaged player sacrifice half their non-Demon permanents")
    void combatDamageSacrificesHalfNonDemonPermanentsOfDamagedPlayer() {
        Permanent rakdos = addCreatureReady(player1, new RakdosTheDefiler());
        Permanent enemyDemon = addCreatureReady(player2, new RakdosTheDefiler());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rakdos)));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(enemyBear.getId(), enemyForest.getId())
                .doesNotContain(enemyDemon.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId))
                .containsExactly(enemyDemon.getId(), enemyForest.getId());
    }
}

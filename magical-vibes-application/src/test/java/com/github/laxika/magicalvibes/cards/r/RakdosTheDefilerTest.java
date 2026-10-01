package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosTheDefiler.class, MistralCharger.class, GhostQuarter.class})
class RakdosTheDefilerTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, its controller sacrifices half their non-Demon permanents, rounded up")
    void attacksSacrificeHalfNonDemonPermanents() {
        Permanent rakdos = addCreatureReady(player1, new RakdosTheDefiler());
        Permanent charger1 = addCreatureReady(player1, new MistralCharger());
        Permanent charger2 = addCreatureReady(player1, new MistralCharger());
        Permanent ghostQuarter = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rakdos)));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(charger1.getId(), charger2.getId(), ghostQuarter.getId())
                .doesNotContain(rakdos.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(charger1.getId(), ghostQuarter.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getId))
                .containsExactly(rakdos.getId(), charger2.getId());
    }

    @Test
    @DisplayName("Combat damage makes the damaged player sacrifice half their non-Demon permanents")
    void combatDamageSacrificesHalfNonDemonPermanentsOfDamagedPlayer() {
        Permanent rakdos = addCreatureReady(player1, new RakdosTheDefiler());
        Permanent enemyDemon = addCreatureReady(player2, new RakdosTheDefiler());
        Permanent enemyCharger = addCreatureReady(player2, new MistralCharger());
        Permanent enemyGhostQuarter = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());

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
                .containsExactlyInAnyOrder(enemyCharger.getId(), enemyGhostQuarter.getId())
                .doesNotContain(enemyDemon.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyCharger.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId))
                .containsExactly(enemyDemon.getId(), enemyGhostQuarter.getId());
    }

    @Test
    @DisplayName("Combat damage rounds up when the damaged player has an odd number of non-Demon permanents")
    void combatDamageRoundsUpForOddNumberOfNonDemonPermanents() {
        Permanent rakdos = addCreatureReady(player1, new RakdosTheDefiler());
        Permanent enemyDemon = addCreatureReady(player2, new RakdosTheDefiler());
        Permanent enemyCharger1 = addCreatureReady(player2, new MistralCharger());
        Permanent enemyCharger2 = addCreatureReady(player2, new MistralCharger());
        Permanent enemyGhostQuarter = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rakdos)));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(enemyCharger1.getId(), enemyCharger2.getId(), enemyGhostQuarter.getId())
                .doesNotContain(enemyDemon.getId());

        harness.handleMultiplePermanentsChosen(player2,
                List.of(enemyCharger1.getId(), enemyGhostQuarter.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId))
                .containsExactly(enemyDemon.getId(), enemyCharger2.getId());
    }
}

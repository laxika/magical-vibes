package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoggAssassinTest extends BaseCardTest {

    @Test
    void opponentChoosesTheSecondTargetBeforeTheAbilityIsActivated() {
        Permanent assassin = addCreatureReady(player1, new MoggAssassin());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, firstTarget.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, secondTarget.getId());
        harness.passBothPriorities();

        boolean firstRemains = gameData.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(firstTarget.getId()));
        boolean secondRemains = gameData.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(secondTarget.getId()));
        assertThat(firstRemains).isNotEqualTo(secondRemains);
        assertThat(assassin.isTapped()).isTrue();
    }

    @Test
    void firstTargetMustBeAnOpponentsCreature() {
        addCreatureReady(player1, new MoggAssassin());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

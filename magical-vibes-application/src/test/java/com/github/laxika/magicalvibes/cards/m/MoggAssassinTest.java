package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggAssassin.class, MonstrousHound.class, CityOfTraitors.class})
class MoggAssassinTest extends BaseCardTest {

    @Test
    void opponentChoosesTheSecondTargetBeforeTheAbilityIsActivated() {
        Permanent assassin = addCreatureReady(player1, new MoggAssassin());
        Permanent firstTarget = addCreatureReady(player2, new MonstrousHound());
        Permanent secondTarget = addCreatureReady(player1, new MonstrousHound());

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
    void opponentMayChooseTheFirstTargetAgain() {
        addCreatureReady(player1, new MoggAssassin());
        Permanent firstTarget = addCreatureReady(player2, new MonstrousHound());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.handlePermanentChosen(player2, firstTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Monstrous Hound");
        harness.assertInGraveyard(player2, "Monstrous Hound");
    }

    @Test
    void opponentCannotChooseANoncreatureAsTheSecondTarget() {
        addCreatureReady(player1, new MoggAssassin());
        Permanent firstTarget = addCreatureReady(player2, new MonstrousHound());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        Permanent validSecondTarget = addCreatureReady(player1, new MonstrousHound());

        harness.activateAbility(player1, 0, null, firstTarget.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, validSecondTarget.getId());
        harness.passBothPriorities();
    }

    @Test
    void firstTargetMustBeAnOpponentsCreature() {
        addCreatureReady(player1, new MoggAssassin());
        Permanent ownCreature = addCreatureReady(player1, new MonstrousHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

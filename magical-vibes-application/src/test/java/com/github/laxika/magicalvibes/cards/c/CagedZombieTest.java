package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CagedZombie.class, VillageRites.class})
class CagedZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without morbid")
    void cannotActivateWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent zombie = addCreatureReady(player1, new CagedZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int zombieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);

        assertThatThrownBy(() -> harness.activateAbility(player1, zombieIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Morbid");
    }

    @Test
    @DisplayName("Each opponent loses 2 life when morbid is met")
    void eachOpponentLosesTwoLifeWhenMorbidIsMet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent zombie = addCreatureReady(player1, new CagedZombie());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int zombieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);
        harness.activateAbility(player1, zombieIndex, null, null);
        assertThat(zombie.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent's sacrifice enables activation on their turn before the spell resolves")
    void opponentsCreatureDeathEnablesActivationOnTheirTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new CagedZombie());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new CagedZombie());
        harness.setHand(player2, List.of(new VillageRites()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstantWithSacrifice(player2, 0, null, sacrifice.getId());
        harness.assertInGraveyard(player2, "Caged Zombie");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability resolves after Caged Zombie is sacrificed in response")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent zombie = addCreatureReady(player1, new CagedZombie());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new VillageRites()));
        harness.castInstantWithSacrifice(player1, 0, null, zombie.getId());
        harness.assertNotOnBattlefield(player1, "Caged Zombie");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A summoning-sick Caged Zombie cannot pay the tap cost even after a creature died")
    void summoningSicknessPreventsActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new CagedZombie());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}

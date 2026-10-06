package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skygames.class, SimicGuildgate.class, GreensideWatcher.class})
class SkygamesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land taps to give target creature flying")
    void enchantedLandGrantsFlying() {
        Permanent forest = attachSkygames(player1);
        Permanent bears = addCreatureReady(player1, new GreensideWatcher());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        Permanent forest = attachSkygames(player1);
        Permanent bears = addCreatureReady(player1, new GreensideWatcher());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void flyingExpiresAtEndOfTurn() {
        attachSkygames(player1);
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void canTargetOpponentsCreatureDuringPostcombatMain() {
        Permanent land = attachSkygames(player1);
        Permanent creature = addCreatureReady(player2, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent land = attachSkygames(player1);
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        Permanent land = attachSkygames(player1);
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void cannotTargetLand() {
        Permanent land = attachSkygames(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLandCannotPayActivationCost() {
        Permanent land = attachSkygames(player1);
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        land.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void activatedAbilityResolvesAfterAuraLeavesBattlefield() {
        attachSkygames(player1);
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, creature.getId());

        Permanent aura = findPermanent(player1, "Skygames");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantingOpponentsLandGrantsAbilityToItsController() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        Permanent creature = addCreatureReady(player1, new GreensideWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skygames()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Skygames").getAttachedTo()).isEqualTo(land.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    private Permanent attachSkygames(Player player) {
        Permanent forest = harness.addToBattlefieldAndReturn(player, new SimicGuildgate());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Skygames());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}

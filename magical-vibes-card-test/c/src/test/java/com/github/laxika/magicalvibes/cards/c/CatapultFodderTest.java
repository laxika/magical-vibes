package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultFodder.class, CatapultCaptain.class, GrizzlyBears.class, WallOfAir.class})
class CatapultFodderTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at beginning of combat with three creatures whose toughness exceeds power")
    void transformsWithThreeHighToughnessCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        addWallOfAir(player1);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(fodder.isTransformed()).isTrue();
        assertThat(fodder.getCard()).isInstanceOf(CatapultCaptain.class);
    }

    @Test
    @DisplayName("Does not transform when fewer than three creatures have greater toughness than power")
    void doesNotTransformWithFewerThanThreeHighToughnessCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at beginning of combat on an opponent's turn")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        addWallOfAir(player1);

        advanceToCombat(player2);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The back face makes an opponent lose life equal to the sacrificed creature's toughness")
    void backFaceLosesLifeEqualToSacrificedToughness() {
        Permanent captain = addTransformedFodder(player1);
        harness.addToBattlefield(player1, new WallOfAir());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(captain.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Wall of Air");
    }

    @Test
    @DisplayName("The back face can target only an opponent")
    void backFaceRejectsNonOpponentTarget() {
        addTransformedFodder(player1);
        harness.addToBattlefield(player1, new WallOfAir());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rechecks the creature count when the transform trigger resolves")
    void doesNotTransformIfConditionStopsBeingTrue() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfAir());
        addWallOfAir(player1);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);
        harness.passBothPriorities();

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled creatures do not count toward the transform condition")
    void doesNotCountOpponentsCreatures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        addWallOfAir(player1);
        addWallOfAir(player2);

        advanceToCombat(player1);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses current power and toughness when checking the transform condition")
    void doesNotCountCreatureWhoseModifiedPowerEqualsToughness() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfAir());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);
        addWallOfAir(player1);

        advanceToCombat(player1);

        assertThat(fodder.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and includes toughness from counters")
    void usesSacrificedCreaturesModifiedToughness() {
        addTransformedFodder(player1);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CatapultFodder());
        fodder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Catapult Fodder");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The Captain cannot sacrifice itself when it is the only creature controlled")
    void cannotSacrificeItself() {
        Permanent captain = addTransformedFodder(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Transforming does not remove summoning sickness for the tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent captain = addTransformedFodder(player1);
        captain.setSummoningSick(true);
        harness.addToBattlefield(player1, new CatapultFodder());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotInGraveyard(player1, "Catapult Fodder");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTransformedFodder(Player player) {
        CatapultFodder card = new CatapultFodder();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void addWallOfAir(Player player) {
        Permanent wall = harness.addToBattlefieldAndReturn(player, new WallOfAir());
        wall.setSummoningSick(false);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}

package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OblivionStrike;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfResurgence.class, Forest.class, OblivionStrike.class, Solemnity.class})
class WallOfResurgenceTest extends BaseCardTest {

    @Test
    void acceptingEtbAbilityPutsCountersOnTargetLandAndAnimatesIt() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getPermanentAnimatedPower()).isEqualTo(0);
        assertThat(land.getPermanentAnimatedToughness()).isEqualTo(0);
        assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(land.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void decliningEtbAbilityLeavesLandUnchanged() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.isPermanentlyAnimated()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void onlyYourOwnLandCanBeTargeted() {
        addLand();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWall();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringWithoutAnOwnLandLeavesNoUnresolvableTrigger() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWall();

        harness.assertOnBattlefield(player1, "Wall of Resurgence");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, opponentLand)).isFalse();
    }

    @Test
    void tappedLandCanBeAnimatedWithoutUntappingIt() {
        Permanent land = addLand();
        land.tap();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.isTapped()).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void anotherWallAddsCountersToTheAlreadyAnimatedLand() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void decliningAnotherWallDoesNotUndoThePreviousAnimation() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void landChangingControllerBeforeResolutionIsNoLongerLegal() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void animationAndHasteSurviveSourceLeavingAndTurnCleanup() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new OblivionStrike()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Wall of Resurgence"));
        harness.assertNotOnBattlefield(player1, "Wall of Resurgence");
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void solemnityMakesTheOptionalCounterPlacementUnavailable() {
        Permanent land = addLand();
        harness.addToBattlefield(player2, new Solemnity());

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    private Permanent addLand() {
        return harness.addToBattlefieldAndReturn(player1, new Forest());
    }

    private void castWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WallOfResurgence(), "{2}{W}");
        harness.passBothPriorities();
    }
}

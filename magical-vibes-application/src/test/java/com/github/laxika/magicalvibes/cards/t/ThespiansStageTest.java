package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThespiansStage.class, Forest.class, Island.class, ScabClanCharger.class, StompingGround.class})
class ThespiansStageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the copy ability makes this land a copy of the target land")
    void becomesCopyOfTargetLand() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("The copy keeps the copy ability and can copy another land afterwards")
    void copyRetainsTheCopyAbility() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();
        assertThat(stage.getCard().getName()).isEqualTo("Forest");

        stage.untap();
        // A basic Forest declares no activated abilities of its own (its mana ability comes from the
        // land type), so the retained copy ability is now the only one on the copy.
        harness.activateAbility(player1, 0, 0, null, island.getId());
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Island");
    }

    @Test
    @DisplayName("The copy ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ThespiansStage());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScabClanCharger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapsForColorlessWithoutUsingTheStack() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingKeepsThePermanentTappedAndGrantsTheLandsManaAbility() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        assertThat(stage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(stage.isTapped()).isTrue();
        stage.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void copyingShockLandDoesNotApplyItsEntryReplacement() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new StompingGround());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        stage.untap();
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Stomping Ground");
        assertThat(stage.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copyAbilityDoesNotResolveWhenItsTargetLeavesTheBattlefield() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        gd.playerBattlefields.get(player2.getId()).remove(forest);
        gd.playerGraveyards.get(player2.getId()).add(forest.getOriginalCard());
        harness.passBothPriorities();

        assertThat(stage.getCard().getName()).isEqualTo("Thespian's Stage");
        assertThat(stage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingSelfThenForestKeepsOnlyTheResolvingCopyAbility() {
        Permanent stage = harness.addToBattlefieldAndReturn(player1, new ThespiansStage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, stage.getId());
        harness.passBothPriorities();
        stage.untap();
        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();
        stage.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid ability index");
        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();
        assertThat(stage.getCard().getName()).isEqualTo("Forest");
    }
}

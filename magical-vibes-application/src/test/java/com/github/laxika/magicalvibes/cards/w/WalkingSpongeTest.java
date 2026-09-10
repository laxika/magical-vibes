package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudOfFaeries;
import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.k.Knighthood;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingSponge.class, CloudOfFaeries.class, Knighthood.class, YavimayaWurm.class,
        FaerieConclave.class})
class WalkingSpongeTest extends BaseCardTest {

    private static final String FLYING_MODE = "It loses flying";
    private static final String FIRST_STRIKE_MODE = "It loses first strike";
    private static final String TRAMPLE_MODE = "It loses trample";

    @Test
    @DisplayName("The ability removes the chosen keyword until end of turn")
    void removesChosenKeyword() {
        Permanent sponge = setUpSponge();
        harness.addToBattlefield(player1, new Knighthood());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CloudOfFaeries());

        activate(sponge, target, FLYING_MODE);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Each keyword can be chosen independently")
    void eachKeywordCanBeChosen() {
        Permanent sponge = setUpSponge();
        harness.addToBattlefield(player1, new Knighthood());
        Permanent firstStrike = harness.addToBattlefieldAndReturn(player1, new CloudOfFaeries());
        Permanent trample = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        assertThat(gqs.hasKeyword(gd, firstStrike, Keyword.FIRST_STRIKE)).isTrue();
        activate(sponge, firstStrike, FIRST_STRIKE_MODE);
        assertThat(gqs.hasKeyword(gd, firstStrike, Keyword.FIRST_STRIKE)).isFalse();

        sponge.untap();
        assertThat(gqs.hasKeyword(gd, trample, Keyword.TRAMPLE)).isTrue();
        activate(sponge, trample, TRAMPLE_MODE);
        assertThat(gqs.hasKeyword(gd, trample, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The chosen keyword returns at end of turn")
    void removalWearsOffAtEndOfTurn() {
        Permanent sponge = setUpSponge();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CloudOfFaeries());

        activate(sponge, target, FLYING_MODE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNoncreature() {
        setUpSponge();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new FaerieConclave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent sponge = setUpSponge();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudOfFaeries());

        activate(sponge, target, FLYING_MODE);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        setUpSponge();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudOfFaeries());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new WalkingSponge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CloudOfFaeries());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("The ability cannot be activated while Walking Sponge is tapped")
    void cannotActivateWhenTapped() {
        Permanent sponge = setUpSponge();
        sponge.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CloudOfFaeries());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent setUpSponge() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new WalkingSponge());
    }

    private void activate(Permanent sponge, Permanent target, String mode) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}

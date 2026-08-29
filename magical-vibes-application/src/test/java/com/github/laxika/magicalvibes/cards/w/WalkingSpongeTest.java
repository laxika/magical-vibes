package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalkingSpongeTest extends BaseCardTest {

    private static final String FLYING_MODE = "It loses flying";
    private static final String FIRST_STRIKE_MODE = "It loses first strike";
    private static final String TRAMPLE_MODE = "It loses trample";

    @Test
    @DisplayName("The ability removes the chosen keyword until end of turn")
    void removesChosenKeyword() {
        Permanent sponge = setUpSponge();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        activate(sponge, target, FLYING_MODE);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each keyword can be chosen independently")
    void eachKeywordCanBeChosen() {
        Permanent sponge = setUpSponge();
        Permanent firstStrike = harness.addToBattlefieldAndReturn(player1, new WhiteKnight());
        Permanent trample = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        activate(sponge, firstStrike, FIRST_STRIKE_MODE);
        assertThat(gqs.hasKeyword(gd, firstStrike, Keyword.FIRST_STRIKE)).isFalse();

        sponge.untap();
        activate(sponge, trample, TRAMPLE_MODE);
        assertThat(gqs.hasKeyword(gd, trample, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The chosen keyword returns at end of turn")
    void removalWearsOffAtEndOfTurn() {
        Permanent sponge = setUpSponge();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

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
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
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

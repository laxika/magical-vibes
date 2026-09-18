package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeerPressure.class, ElvishWarrior.class, GlorySeeker.class, AvianChangeling.class})
class PeerPressureTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of all creatures of the chosen type when you have the most")
    void gainsControlWhenControllerHasMoreOfChosenType() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent secondOpposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent opposingHuman = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        castAndChoose("ELF");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownElf, opposingElf, secondOpposingElf);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opposingElf, secondOpposingElf)
                .contains(opposingHuman);
    }

    @Test
    @DisplayName("Does nothing when the chosen type is tied")
    void doesNothingOnTie() {
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());

        castAndChoose("ELF");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingElf);
    }

    @Test
    @DisplayName("Does nothing when an opponent controls more of the chosen type")
    void doesNothingWhenOpponentHasMore() {
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());

        castAndChoose("ELF");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingElf);
    }

    @Test
    @DisplayName("Counts a changeling as the chosen creature type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castAndChoose("ELF");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingElf);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingElf);
    }

    @Test
    @DisplayName("Control of the chosen creatures lasts indefinitely")
    void controlChangeLastsIndefinitely() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castAndChoose("ELF");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingElf);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingElf);
    }

    private void castAndChoose(String creatureType) {
        harness.castFromHand(player1, new PeerPressure(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SubjugateTheHobbits.class, ElvishWarrior.class, ShivanDragon.class, Mountain.class})
class SubjugateTheHobbitsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of noncommander creatures with mana value 3 or less")
    void gainsControlOfEligibleCreatures() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent tooExpensive = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent ownEligible = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eligible, ownEligible);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tooExpensive, noncreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(eligible);
    }

    @Test
    @DisplayName("Does not gain control of a commander even when it has mana value 3 or less")
    void leavesCommanderUnderItsCurrentControl() {
        ElvishWarrior commanderCard = new ElvishWarrior();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player2, commanderCard);

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(commander);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
    }

    private void castSubjugateTheHobbits() {
        harness.setHand(player1, List.of(new SubjugateTheHobbits()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}

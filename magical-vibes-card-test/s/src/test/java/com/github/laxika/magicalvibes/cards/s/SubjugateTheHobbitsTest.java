package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.ReclamationSage;
import com.github.laxika.magicalvibes.cards.s.SamLoyalAttendant;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SubjugateTheHobbits.class, ElvishWarrior.class, ShivanDragon.class, Mountain.class,
        SamLoyalAttendant.class, ReclamationSage.class, SelflessSquire.class})
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
        SamLoyalAttendant commanderCard = new SamLoyalAttendant();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = harness.addToBattlefieldAndReturn(player2, commanderCard);

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(commander);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    @DisplayName("Gains control of every eligible creature, including mana value three")
    void gainsControlOfMultipleCreaturesAtManaValueBoundary() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ReclamationSage());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ReclamationSage());
        Permanent smaller = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        first.tap();

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second, smaller);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second, smaller);
        assertThat(first.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not gain control of creatures with mana value four")
    void excludesCreatureJustAboveManaValueBoundary() {
        Permanent squire = harness.addToBattlefieldAndReturn(player2, new SelflessSquire());

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(squire);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(squire);
    }

    @Test
    @DisplayName("Gains control of legendary creatures that are not commanders")
    void gainsControlOfNoncommanderLegendaryCreature() {
        Permanent sam = harness.addToBattlefieldAndReturn(player2, new SamLoyalAttendant());

        castSubjugateTheHobbits();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sam);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sam);
    }

    @Test
    @DisplayName("Control persists into the next turn")
    void controlDoesNotExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castSubjugateTheHobbits();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Resolves without eligible creatures")
    void resolvesWithoutEligibleCreatures() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        castSubjugateTheHobbits();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragon, land);
        harness.assertInGraveyard(player1, "Subjugate the Hobbits");
    }

    private void castSubjugateTheHobbits() {
        harness.castFromHand(player1, new SubjugateTheHobbits(), "{5}{U}{U}");
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VoiceOfTheProvinces;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearchlightGeist.class, VoiceOfTheProvinces.class})
class SearchlightGeistTest extends BaseCardTest {

    @Test
    @DisplayName("{3}{B}: gains deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent geist = addCreatureReady(player1, new SearchlightGeist());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(geist.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent geist = addCreatureReady(player1, new SearchlightGeist());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(geist.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(geist.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new SearchlightGeist());
        geist.setSummoningSick(true);
        geist.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(geist.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
        harness.passBothPriorities();

        assertThat(geist.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(geist.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        Permanent geist = addCreatureReady(player1, new SearchlightGeist());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(geist.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void grantsDeathtouchOnlyToTheActivatedSource() {
        Permanent geist = addCreatureReady(player1, new SearchlightGeist());
        Permanent otherGeist = addCreatureReady(player1, new SearchlightGeist());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(geist.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(otherGeist.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    void activatedDeathtouchKillsALargerFlyingBlocker() {
        Permanent geist = addCreatureReady(player1, new SearchlightGeist());
        Permanent blocker = addCreatureReady(player2, new VoiceOfTheProvinces());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        geist.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.assertInGraveyard(player1, "Searchlight Geist");
        harness.assertInGraveyard(player2, "Voice of the Provinces");
        harness.assertNotOnBattlefield(player2, "Voice of the Provinces");
    }
}

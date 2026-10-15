package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.cards.v.VodalianZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgShambler.class, VodalianZombie.class, RazorfootGriffin.class})
class UrborgShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures get -1/-1 regardless of controller")
    void debuffsOtherBlackCreatures() {
        harness.addToBattlefield(player1, new UrborgShambler());
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new VodalianZombie());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new VodalianZombie());

        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Urborg Shambler does not debuff itself")
    void doesNotDebuffItself() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new UrborgShambler());

        assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shambler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Shamblers debuff each other once")
    void multipleSourcesDebuffEachOther() {
        harness.addToBattlefield(player1, new UrborgShambler());
        harness.addToBattlefield(player1, new UrborgShambler());

        for (Permanent shambler : findPermanents(player1, "Urborg Shambler")) {
            assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, shambler)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Nonblack creatures are unaffected")
    void nonblackCreaturesAreUnaffected() {
        harness.addToBattlefield(player1, new UrborgShambler());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Black creatures entering later are immediately reduced")
    void affectsBlackCreaturesEnteringLater() {
        harness.enterBattlefieldAndReturn(player1, new UrborgShambler());

        Permanent zombie = harness.enterBattlefieldAndReturn(player2, new VodalianZombie());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Shamblers reduce other black creatures to zero toughness")
    void stackedReductionsKillOtherBlackCreatures() {
        harness.addToBattlefield(player1, new VodalianZombie());
        harness.addToBattlefield(player2, new VodalianZombie());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new UrborgShambler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new UrborgShambler());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Vodalian Zombie");
        harness.assertNotOnBattlefield(player2, "Vodalian Zombie");
        harness.assertInGraveyard(player1, "Vodalian Zombie");
        harness.assertInGraveyard(player2, "Vodalian Zombie");
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderManifestation.class, GrizzlyBears.class, HillGiant.class})
class SpiderManifestationTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds red mana")
    void tapAbilityAddsRedMana() {
        Permanent spider = addReadySpider(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds green mana")
    void tapAbilityAddsGreenMana() {
        Permanent spider = addReadySpider(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater untaps Spider Manifestation")
    void highManaValueSpellUntapsSpiderManifestation() {
        Permanent spider = addReadySpider(player1);
        spider.tap();
        prepareMainPhase();
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not untap Spider Manifestation")
    void lowManaValueSpellDoesNotUntapSpiderManifestation() {
        Permanent spider = addReadySpider(player1);
        spider.tap();
        prepareMainPhase();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's qualifying spell does not untap your Spider")
    void opponentsSpellDoesNotUntapSpider() {
        Permanent spider = addReadySpider(player1);
        spider.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each controlled Spider untaps without untapping an opponent's Spider")
    void eachControlledSpiderUntaps() {
        Permanent first = addReadySpider(player1);
        Permanent second = addReadySpider(player1);
        Permanent opponentsSpider = addReadySpider(player2);
        first.tap();
        second.tap();
        opponentsSpider.tap();
        prepareMainPhase();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opponentsSpider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Spider tapped after casting untaps when its trigger resolves")
    void canTapForManaBeforeUntapTriggerResolves() {
        Permanent spider = addReadySpider(player1);
        prepareMainPhase();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        assertThat(spider.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(spider.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();

        harness.passBothPriorities();

        assertThat(spider.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(spider.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
    }

    private Permanent addReadySpider(Player player) {
        return addCreatureReady(player, new SpiderManifestation());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}

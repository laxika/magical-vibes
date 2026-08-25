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

import java.util.List;

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
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not untap Spider Manifestation")
    void lowManaValueSpellDoesNotUntapSpiderManifestation() {
        Permanent spider = addReadySpider(player1);
        spider.tap();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
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

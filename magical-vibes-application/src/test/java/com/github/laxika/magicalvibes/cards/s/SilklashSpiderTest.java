package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilklashSpider.class, StormCrow.class, GlorySeeker.class})
class SilklashSpiderTest extends BaseCardTest {

    private Permanent addSpider(Player player) {
        return addCreatureReady(player, new SilklashSpider());
    }

    @Test
    @DisplayName("Ability deals X damage to each creature with flying, killing them")
    void killsFlyingCreatures() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 4); // {2}{G}{G} → X=2

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Storm Crow");
    }

    @Test
    @DisplayName("Ability does not damage non-flying creatures")
    void doesNotDamageNonFlyers() {
        addSpider(player1);
        addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.GREEN, 5); // {3}{G}{G} → X=3

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Ability does not damage players")
    void doesNotDamagePlayers() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ability with X=1 does not kill a 2-toughness flyer")
    void xOneLeavesToughFlyerAlive() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 3); // {1}{G}{G} → X=1

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Storm Crow");
    }

    @Test
    @DisplayName("Ability damages flying creatures controlled by either player")
    void damagesFlyingCreaturesOnBothBattlefields() {
        addSpider(player1);
        addCreatureReady(player1, new StormCrow());
        addCreatureReady(player2, new StormCrow());
        addCreatureReady(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Storm Crow");
        harness.assertNotOnBattlefield(player2, "Storm Crow");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Generic mana can pay X while green mana pays the colored cost")
    void genericManaCanPayX() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
    }

    @Test
    @DisplayName("Cannot activate without enough mana for GG")
    void cannotActivateWithoutEnoughMana() {
        addSpider(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability with X=0 deals no damage")
    void xZeroDealsNoDamage() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Storm Crow");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Spider can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SilklashSpider());
        spider.setSummoningSick(true);
        spider.tap();
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
        harness.assertOnBattlefield(player1, "Silklash Spider");
    }

    @Test
    @DisplayName("Repeated activations accumulate damage on surviving flyers")
    void repeatedActivationsAccumulateDamage() {
        addSpider(player1);
        addCreatureReady(player2, new StormCrow());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Storm Crow");

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
        harness.assertOnBattlefield(player1, "Silklash Spider");
    }

    @Test
    @DisplayName("Ability affects flyers that enter after activation but before resolution")
    void includesFlyersEnteringBeforeResolution() {
        addSpider(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 2, null);
        harness.addToBattlefield(player2, new StormCrow());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Storm Crow");
        harness.assertOnBattlefield(player1, "Silklash Spider");
    }
}

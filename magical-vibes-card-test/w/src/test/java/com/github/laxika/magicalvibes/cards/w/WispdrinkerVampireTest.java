package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WispdrinkerVampire.class, GrizzlyBears.class, HillGiant.class, GloriousAnthem.class})
class WispdrinkerVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Drains each opponent when another small creature enters under your control")
    void drainsWhenSmallAllyEnters() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger for a large creature or for itself")
    void doesNotTriggerForLargeCreatureOrItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new WispdrinkerVampire(), "{2}{W}{B}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability grants deathtouch and lifelink only to creatures with power 2 or less")
    void activatedAbilityGrantsKeywordsToSmallCreatures() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new WispdrinkerVampire());
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vampire.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(vampire.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(smallCreature.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(smallCreature.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(largeCreature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(largeCreature.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Checks effective power including continuous bonuses when a creature enters")
    void doesNotDrainForCreatureEnteringWithPowerAboveTwo() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not drain when an opponent's small creature enters")
    void doesNotDrainForOpponentCreature() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers for every small creature rather than only once per turn")
    void drainsForEachSmallCreature() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Drain still resolves after the entering creature's power increases")
    void doesNotRecheckPowerOnResolution() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Keyword grant retains its affected creatures and expires at end of turn")
    void keywordGrantUsesResolutionSnapshotAndExpires() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new WispdrinkerVampire());
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, smallCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, smallCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.LIFELINK)).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, smallCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, smallCreature, Keyword.LIFELINK)).isFalse();
    }
}

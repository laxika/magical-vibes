package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WispdrinkerVampire.class, GrizzlyBears.class, HillGiant.class})
class WispdrinkerVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Drains each opponent when another small creature enters under your control")
    void drainsWhenSmallAllyEnters() {
        harness.addToBattlefield(player1, new WispdrinkerVampire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new WispdrinkerVampire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
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
}

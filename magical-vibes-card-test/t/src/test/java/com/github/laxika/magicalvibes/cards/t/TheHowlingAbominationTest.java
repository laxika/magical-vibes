package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHowlingAbomination.class, Shock.class, ProdigalPyromancer.class})
class TheHowlingAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Gains trample after its controller casts three spells this turn")
    void gainsTrampleAfterThreeSpells() {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +2/+2 and deals 2 damage to each opponent when targeted by a spell")
    void triggersWhenTargetedBySpell() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, abomination.getId());
        harness.passBothPriorities();

        assertThat(abomination.getEffectivePower()).isEqualTo(7);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(7);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when targeted by an ability")
    void doesNotTriggerWhenTargetedByAbility() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, abomination.getId());
        harness.passBothPriorities();

        assertThat(abomination.getEffectivePower()).isEqualTo(5);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample applies as soon as the third spell is cast and expires next turn")
    void trampleDoesNotWaitForResolutionAndResetsNextTurn() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isFalse();

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isTrue();
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's three spells do not grant trample")
    void opponentsSpellsDoNotGrantTrample() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.hasKeyword(gd, abomination, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Its controller's targeting spells each trigger and the boosts expire at end of turn")
    void ownSpellsTriggerAndBoostsAccumulateUntilEndOfTurn() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, abomination.getId());
        harness.passBothPriorities();
        assertThat(abomination.getEffectivePower()).isEqualTo(7);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, abomination.getId());
        harness.passBothPriorities();
        assertThat(abomination.getEffectivePower()).isEqualTo(9);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(9);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(abomination.getEffectivePower()).isEqualTo(5);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @CardUsed({Humble.class})
    @DisplayName("Does not trigger for a new targeting spell after losing its abilities")
    void losingAbilitiesPreventsSubsequentTargetTrigger() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new TheHowlingAbomination());
        harness.setHand(player2, List.of(new Humble(), new Shock()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, abomination.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        assertThat(abomination.getEffectivePower()).isEqualTo(2);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(3);

        harness.castInstant(player2, 0, abomination.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(abomination.getEffectivePower()).isEqualTo(2);
        assertThat(abomination.getEffectiveToughness()).isEqualTo(3);
    }
}

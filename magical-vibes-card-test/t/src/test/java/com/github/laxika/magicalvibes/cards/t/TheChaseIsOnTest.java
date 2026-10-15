package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheChaseIsOn.class, Forest.class, MarketwatchPhantom.class})
class TheChaseIsOnTest extends BaseCardTest {

    @Test
    @DisplayName("Repeated casts on your own creature stack the boost and each investigate")
    void repeatedCastsOnOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MarketwatchPhantom());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new TheChaseIsOn(), new TheChaseIsOn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Gives target creature +3/+0 and first strike, then investigates")
    void boostsGrantsFirstStrikeAndInvestigates() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new TheChaseIsOn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The boost and first strike wear off at cleanup, but the Clue remains")
    void temporaryEffectsWearOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new TheChaseIsOn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TheChaseIsOn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not investigate when its only target leaves the battlefield")
    void doesNotInvestigateWhenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new TheChaseIsOn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "The Chase Is On");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MarketwatchPhantom());
        harness.setHand(player1, List.of(new TheChaseIsOn()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

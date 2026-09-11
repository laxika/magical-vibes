package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnstableShapeshifter.class, JackalPup.class, WindDrake.class})
class UnstableShapeshifterTest extends BaseCardTest {

    private Permanent putShapeshifter() {
        return harness.addToBattlefieldAndReturn(player1, new UnstableShapeshifter());
    }

    @Test
    @DisplayName("Becomes a copy of another creature that enters")
    void becomesCopyOfEnteringCreature() {
        Permanent shifter = putShapeshifter();

        harness.castFromHand(player1, new WindDrake(), "{2}{U}");
        harness.passBothPriorities(); // Wind Drake resolves, trigger goes on the stack
        harness.passBothPriorities(); // become-copy resolves

        assertThat(shifter.getCard().getName()).isEqualTo("Wind Drake");
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Retains the copy ability — copies again when another creature enters")
    void retainsAbilityAndCopiesAgain() {
        Permanent shifter = putShapeshifter();

        harness.castFromHand(player1, new JackalPup(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(shifter.getCard().getName()).isEqualTo("Jackal Pup");

        harness.castFromHand(player1, new WindDrake(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isEqualTo("Wind Drake");
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Copies a creature entering under an opponent's control")
    void copiesOpponentCreature() {
        Permanent shifter = putShapeshifter();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WindDrake(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isEqualTo("Wind Drake");
    }

    @Test
    @DisplayName("Does not trigger on itself entering")
    void doesNotTriggerOnItself() {
        harness.castFromHand(player1, new UnstableShapeshifter(), "{3}{U}");
        harness.passBothPriorities();

        Permanent shifter = findPermanent(player1, "Unstable Shapeshifter");
        assertThat(shifter.getCard().getName()).isEqualTo("Unstable Shapeshifter");
    }

    @Test
    @DisplayName("Uses the entering creature's last-known information if it leaves before resolution")
    void usesLastKnownInformationIfEnteringCreatureLeaves() {
        Permanent shifter = putShapeshifter();

        harness.castFromHand(player1, new WindDrake(), "{2}{U}");
        harness.passBothPriorities();

        Permanent entering = findPermanent(player1, "Wind Drake");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, entering));
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isEqualTo("Wind Drake");
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shifter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Copies a creature's face-down characteristics")
    void copiesFaceDownCharacteristics() {
        Permanent shifter = putShapeshifter();

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WindDrake());
        entering.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.passBothPriorities();

        assertThat(shifter.getCard().getName()).isNull();
        assertThat(gqs.getEffectivePower(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shifter)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, shifter)).isEmpty();
        assertThat(gqs.hasKeyword(gd, shifter, Keyword.FLYING)).isFalse();
    }
}

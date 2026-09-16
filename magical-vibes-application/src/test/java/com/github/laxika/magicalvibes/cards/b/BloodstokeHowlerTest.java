package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KeeneyeAven;
import com.github.laxika.magicalvibes.cards.n.NeedleshotGourna;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodstokeHowler.class, KeeneyeAven.class, NeedleshotGourna.class})
class BloodstokeHowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Bloodstoke Howler face up boosts your Beasts until end of turn")
    void turningFaceUpBoostsOwnBeastsOnly() {
        Permanent otherBeast = harness.addToBattlefieldAndReturn(player1, new NeedleshotGourna());
        Permanent nonBeast = harness.addToBattlefieldAndReturn(player1, new KeeneyeAven());
        Permanent opponentBeast = harness.addToBattlefieldAndReturn(player2, new NeedleshotGourna());
        Permanent howler = castFaceDown();
        assertThat(howler.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(howler));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherBeast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherBeast)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonBeast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonBeast)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBeast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBeast)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherBeast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherBeast)).isEqualTo(6);
    }

    @Test
    @DisplayName("The face-up ability does not boost a Beast that enters later")
    void faceUpBoostDoesNotAffectLaterBeasts() {
        Permanent howler = castFaceDown();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(howler));
        harness.passBothPriorities();

        Permanent laterBeast = harness.addToBattlefieldAndReturn(player1, new NeedleshotGourna());

        assertThat(gqs.getEffectivePower(gd, laterBeast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterBeast)).isEqualTo(6);
    }

    @Test
    @DisplayName("Casting Bloodstoke Howler face up does not trigger its face-up ability")
    void castingFaceUpDoesNotTriggerAbility() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new NeedleshotGourna());

        harness.castFromHand(player1, new BloodstokeHowler(), "{5}{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(6);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new BloodstokeHowler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Bloodstoke Howler");
    }
}

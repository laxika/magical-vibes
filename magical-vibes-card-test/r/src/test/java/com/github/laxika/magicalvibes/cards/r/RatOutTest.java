package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatOut.class, GrizzlyBears.class})
class RatOutTest extends BaseCardTest {

    @Test
    @DisplayName("Rat Out gives a target creature -1/-1 and creates a Rat that can't block")
    void debuffsTargetAndCreatesNonBlockingRat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        assertThat(bls.canBlock(gd, rat)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rat Out creates a Rat when no creature is chosen")
    void createsRatWithoutTarget() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Rat Out creates exactly one black 1/1 Rat creature token")
    void createsCorrectToken() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getCard().isToken()).isTrue();
        assertThat(rat.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(rat.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(rat.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
        assertThat(countPermanents(player2, "Rat")).isZero();
    }

    @Test
    @DisplayName("Rat Out may omit a target even when creatures are present")
    void canOmitTargetWithCreaturePresent() {
        harness.setHand(player1, List.of(new RatOut(), new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        Permanent originalRat = findPermanent(player1, "Rat");

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, originalRat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, originalRat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Out can kill your own 1/1 Rat and still create a new Rat")
    void canTargetOwnCreatureAndCreatesTokenAfterLethalDebuff() {
        harness.setHand(player1, List.of(new RatOut(), new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        UUID originalId = findPermanent(player1, "Rat").getId();

        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        Permanent newRat = findPermanent(player1, "Rat");
        assertThat(newRat.getId()).isNotEqualTo(originalId);
        assertThat(gqs.getEffectiveToughness(gd, newRat)).isEqualTo(1);
        assertThat(bls.canBlock(gd, newRat)).isFalse();
    }

    @Test
    @DisplayName("Rat Out creates no token when its only chosen target leaves before resolution")
    void createsNoTokenWhenChosenTargetLeaves() {
        harness.setHand(player1, List.of(new RatOut(), new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        Permanent rat = findPermanent(player1, "Rat");

        harness.castInstant(player1, 0, rat.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rat);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Rat")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof RatOut).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}

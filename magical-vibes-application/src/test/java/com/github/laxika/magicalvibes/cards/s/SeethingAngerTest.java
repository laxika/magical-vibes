package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeethingAnger.class, SkyshroudFalcon.class, Shock.class})
class SeethingAngerTest extends BaseCardTest {

    @Test
    @DisplayName("Seething Anger gives the target creature +3/+0")
    void boostsTarget() {
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Skyshroud Falcon"));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Skyshroud Falcon"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Skyshroud Falcon"))).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Skyshroud Falcon"));

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Skyshroud Falcon"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Skyshroud Falcon"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying buyback returns Seething Anger to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithBuyback(player1, 0, harness.getPermanentId(player1, "Skyshroud Falcon"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Seething Anger");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Seething Anger");
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Skyshroud Falcon"))).isEqualTo(4);
    }

    @Test
    @DisplayName("Not paying buyback puts Seething Anger into its owner's graveyard")
    void unpaidBuybackGoesToGraveyard() {
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Skyshroud Falcon"));

        harness.assertInGraveyard(player1, "Seething Anger");
        harness.assertNotInHand(player1, "Seething Anger");
    }

    @Test
    @DisplayName("Buyback does not return a spell whose only target becomes illegal")
    void buybackDoesNotReturnWhenTargetBecomesIllegal() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithBuyback(player1, 0, falcon.getId());
        harness.castInstant(player2, 0, falcon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Seething Anger");
        harness.assertNotInHand(player1, "Seething Anger");
        harness.assertInGraveyard(player1, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("Seething Anger cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AwakenTheErstwhileTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and creates Zombies equal to their own discard count")
    void discardsHandsAndCreatesPerPlayerZombieCounts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new AwakenTheErstwhile(), new GrizzlyBears(), new Mountain()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(countPermanents(player2, "Zombie")).isEqualTo(4);
        assertThat(findPermanents(player1, "Zombie"))
                .allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)
                        && permanent.getEffectivePower() == 2
                        && permanent.getEffectiveToughness() == 2);
        assertThat(findPermanents(player2, "Zombie"))
                .allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)
                        && permanent.getEffectivePower() == 2
                        && permanent.getEffectiveToughness() == 2);
    }
}

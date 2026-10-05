package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArchersParapet;
import com.github.laxika.magicalvibes.cards.d.DisownedAncestor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinTreeInvocation.class, ArchersParapet.class, DisownedAncestor.class})
class KinTreeInvocationTest extends BaseCardTest {

    private List<Permanent> spiritWarriors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit Warrior"))
                .toList();
    }

    @Test
    void createsOneTokenWithPowerAndToughnessEqualToGreatestControlledToughness() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player1, new ArchersParapet());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(spiritWarriors()).singleElement()
                .satisfies(token -> assertThat(token.getCard().getPower()).isEqualTo(5))
                .satisfies(token -> assertThat(token.getCard().getToughness()).isEqualTo(5));
    }

    @Test
    void opponentCreaturesDoNotCount() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new ArchersParapet());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(spiritWarriors()).isEmpty();
    }

    @Test
    void usesGreatestEffectiveToughnessRatherThanPrintedToughnessOrTotal() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player1, new ArchersParapet());
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new DisownedAncestor());
        ancestor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(spiritWarriors()).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(6);
            assertThat(token.getCard().getToughness()).isEqualTo(6);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.SPIRIT, CardSubtype.WARRIOR);
        });
    }

    @Test
    void determinesSizeAtResolutionAndKeepsThatSizeAfterward() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new DisownedAncestor());

        harness.castSorcery(player1, 0, 0);
        ancestor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();
        ancestor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(spiritWarriors()).singleElement().satisfies(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        });
    }

    @Test
    void zeroToughnessTokenDoesNotSurviveWhenNoCreaturesAreControlled() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(spiritWarriors()).isEmpty();
    }
}

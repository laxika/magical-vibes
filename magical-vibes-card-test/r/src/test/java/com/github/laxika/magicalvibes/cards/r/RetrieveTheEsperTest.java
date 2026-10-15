package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetrieveTheEsper.class})
class RetrieveTheEsperTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast creates a 3/3 Robot Warrior without counters")
    void normalCastCreatesRobotWarriorWithoutCounters() {
        harness.setHand(player1, List.of(new RetrieveTheEsper()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = findPermanent(player1, "Robot Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flashback creates a Robot Warrior with two +1/+1 counters")
    void flashbackCreatesRobotWarriorWithTwoCounters() {
        harness.setGraveyard(player1, List.of(new RetrieveTheEsper()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        Permanent token = findPermanent(player1, "Robot Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Retrieve the Esper");
    }

    @Test
    @DisplayName("Flashback after a normal cast adds counters only to the new token and exiles the spell")
    void flashbackAfterNormalCastOnlyCountersNewToken() {
        RetrieveTheEsper spell = new RetrieveTheEsper();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent originalToken = findPermanent(player1, "Robot Warrior");
        harness.assertInGraveyard(player1, "Retrieve the Esper");

        harness.castAndResolveFlashback(player1, 0, null);

        List<Permanent> tokens = findPermanents(player1, "Robot Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(originalToken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newToken = tokens.stream()
                .filter(token -> !token.getId().equals(originalToken.getId()))
                .findFirst().orElseThrow();
        assertThat(newToken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(newToken.getEffectivePower()).isEqualTo(5);
        assertThat(newToken.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Retrieve the Esper");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(spell.getId());
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(findPermanents(player2, "Robot Warrior")).isEmpty();
    }
}

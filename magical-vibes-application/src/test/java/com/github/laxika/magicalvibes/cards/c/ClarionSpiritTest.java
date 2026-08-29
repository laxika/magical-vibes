package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClarionSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Spirit token for your second spell each turn")
    void createsTokenForSecondSpell() {
        addCreatureReady(player1, new ClarionSpirit());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts their second spell")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new ClarionSpirit());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }
}

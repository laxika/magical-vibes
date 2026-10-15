package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AlignedHeart.class, LightningBolt.class})
class AlignedHeartTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell adds a rally counter and creates that many Monk tokens")
    void flurryAddsCounterAndCreatesTokens() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new AlignedHeart());
        harness.setHand(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(heart.getCounterCount(CounterType.RALLY)).isZero();
        assertThat(countPermanents(player1, "Monk")).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(heart.getCounterCount(CounterType.RALLY)).isEqualTo(1);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);

        Permanent monk = findPermanent(player1, "Monk");
        assertThat(gqs.hasKeyword(gd, monk, Keyword.PROWESS)).isTrue();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rally counters persist and increase the next turn's token count")
    void rallyCountersPersistAcrossTurns() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new AlignedHeart());
        harness.setHand(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        castTwoSpells();
        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();

        harness.castInstant(player1, 0, player2.getId());
        do {
            harness.passBothPriorities();
        } while (!gd.stack.isEmpty());
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
        harness.castInstant(player1, 0, player2.getId());
        do {
            harness.passBothPriorities();
        } while (!gd.stack.isEmpty());
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(2);

        assertThat(heart.getCounterCount(CounterType.RALLY)).isEqualTo(2);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(3);
    }

    private void castTwoSpells() {
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    @Test
    void triggersOnOpponentsTurnButOnlyForControllersSpells() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new AlignedHeart());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(heart.getCounterCount(CounterType.RALLY)).isZero();
        assertThat(countPermanents(player1, "Monk")).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(heart.getCounterCount(CounterType.RALLY)).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(heart.getCounterCount(CounterType.RALLY)).isEqualTo(1);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(1);
        Permanent monk = findPermanent(player1, "Monk");
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    void enteringAsSecondSpellDoesNotTriggerForItselfOrThirdSpell() {
        harness.setHand(player1, List.of(new LightningBolt(), new AlignedHeart(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent heart = findPermanent(player1, "Aligned Heart");
        assertThat(heart.getCounterCount(CounterType.RALLY)).isZero();
        assertThat(countPermanents(player1, "Monk")).isZero();
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistralSinger.class, DrowsingTyrannodon.class, Shock.class})
class MistralSingerTest extends BaseCardTest {

    private Permanent addSinger() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new MistralSinger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return singer;
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent singer = addSinger();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent singer = addSinger();

        harness.setHand(player1, List.of(new DrowsingTyrannodon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent singer = addSinger();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(2);
    }
    @Test
    @DisplayName("Prowess resolves before the triggering spell")
    void prowessResolvesBeforeSpell() {
        Permanent singer = addSinger();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each noncreature spell produces a separate cumulative prowess boost")
    void multipleSpellsProduceCumulativeBoosts() {
        Permanent singer = addSinger();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(4);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent singer = addSinger();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }
}

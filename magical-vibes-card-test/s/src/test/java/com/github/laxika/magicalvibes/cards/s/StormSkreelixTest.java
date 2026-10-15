package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormSkreelix.class, Startle.class, Shock.class, Divination.class, GrizzlyBears.class})
class StormSkreelixTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells you cast cost {1} less")
    void instantAndSorcerySpellsCostOneLess() {
        harness.addToBattlefield(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting an instant gives Storm Skreelix +2/+0 until end of turn")
    void castingInstantBoostsStormSkreelixUntilEndOfTurn() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(2);
    }

    @Test
    void castingSorceryBoostsOnlyPowerBeforeTheSpellResolves() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);

        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skreelix)).isEqualTo(4);
    }

    @Test
    void multipleInstantCastsAccumulateBoosts() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, skreelix)).isEqualTo(4);
    }

    @Test
    void multipleSkreelixesStackCostReductionsAndEachTrigger() {
        var first = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        var second = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
    }

    @Test
    void reductionCannotPayColoredMana() {
        harness.addToBattlefield(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentInstantDoesNotTriggerBoost() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void opponentSorceryDoesNotReceiveCostReduction() {
        harness.addToBattlefield(player1, new StormSkreelix());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void castingCreatureDoesNotTriggerBoost() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void instantCostReductionAndBoostWorkDuringOpponentsTurn() {
        var skreelix = harness.addToBattlefieldAndReturn(player1, new StormSkreelix());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Startle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, skreelix.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, skreelix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skreelix)).isEqualTo(4);
    }
}

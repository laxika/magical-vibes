package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CodespellCleric;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RighteousValkyrie.class, CodespellCleric.class, GrizzlyBears.class, SerraAngel.class})
class RighteousValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the toughness of an entering Angel")
    void gainsLifeForEnteringAngel() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Gains life for an entering Cleric but not a non-Cleric creature")
    void gainsLifeForEnteringCleric() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new CodespellCleric()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Boosts all creatures you control, including Righteous Valkyrie, above the life threshold")
    void boostsAllControlledCreaturesAtThreshold() {
        harness.setLife(player1, 27);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost creatures below the life threshold")
    void doesNotBoostBelowThreshold() {
        harness.setLife(player1, 26);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new RighteousValkyrie()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void angelClericTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new RighteousValkyrie()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    void checksToughnessSeparatelyAsEachTriggerResolves() {
        harness.setLife(player1, 26);
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.setHand(player1, List.of(new CodespellCleric()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 32);
    }

    @Test
    void boostDisappearsImmediatelyWhenLifeDropsBelowThreshold() {
        harness.setLife(player1, 27);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(6);

        harness.setLife(player1, 26);

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        harness.setLife(player1, 27);
        harness.addToBattlefield(player1, new RighteousValkyrie());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RighteousValkyrie());

        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForOpponentsAngelCleric() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RighteousValkyrie()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void commanderThresholdIsSevenAboveForty() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 40);
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new RighteousValkyrie());

        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);

        harness.setLife(player1, 46);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(4);

        harness.setLife(player1, 47);
        assertThat(gqs.getEffectivePower(gd, valkyrie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, valkyrie)).isEqualTo(6);
    }
}

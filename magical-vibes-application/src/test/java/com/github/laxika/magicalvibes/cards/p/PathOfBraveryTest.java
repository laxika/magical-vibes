package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfBravery.class, KalonianTusker.class, DoomBlade.class})
class PathOfBraveryTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures +1/+1 at the starting life total")
    void boostAtStartingLife() {
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KalonianTusker());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost below the starting life total")
    void noBoostBelowStartingLife() {
        gd.playerLifeTotals.put(player1.getId(), 19);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KalonianTusker());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost returns once life climbs back to the starting total")
    void boostIsDynamic() {
        gd.playerLifeTotals.put(player1.getId(), 15);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KalonianTusker());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerLifeTotals.put(player1.getId(), 25);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new KalonianTusker());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with two creatures gains 2 life")
    void gainsLifePerAttacker() {
        harness.addToBattlefield(player1, new PathOfBravery());

        addCreatureReady(player1, new KalonianTusker());
        addCreatureReady(player1, new KalonianTusker());

        gd.playerLifeTotals.put(player1.getId(), 20);

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("No attack trigger when no creatures attack")
    void noTriggerWithoutAttackers() {
        harness.addToBattlefield(player1, new PathOfBravery());
        harness.addToBattlefieldAndReturn(player1, new KalonianTusker());

        gd.playerLifeTotals.put(player1.getId(), 20);

        declareAttackers(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts attackers at resolution after an attacker is destroyed")
    void countsRemainingAttackersAtResolution() {
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent attacker = addCreatureReady(player1, new KalonianTusker());
        addCreatureReady(player1, new KalonianTusker());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Uses the Commander starting life total for the boost")
    void usesActualStartingLifeTotal() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 30);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KalonianTusker());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.setLife(player1, 40);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attack life gain can enable the continuous boost")
    void attackLifeGainEnablesBoost() {
        harness.setLife(player1, 19);
        harness.addToBattlefield(player1, new PathOfBravery());
        Permanent creature = addCreatureReady(player1, new KalonianTusker());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}

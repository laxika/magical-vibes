package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RakdosPitDragon.class, Ragamuffyn.class})
class RakdosPitDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying until end of turn")
    void gainsFlyingUntilEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 until end of turn")
    void getsPlusOnePowerUntilEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        int basePower = gqs.getEffectivePower(gd, dragon);
        int baseToughness = gqs.getEffectiveToughness(gd, dragon);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The +1/+0 bonus expires at the end of the turn")
    void pumpBonusExpiresAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        int basePower = gqs.getEffectivePower(gd, dragon);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Has double strike with an empty hand")
    void hasDoubleStrikeWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike when a card enters its controller's hand")
    void losesDoubleStrikeWhenHandGrows() {
        harness.setHand(player1, List.of());
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setHand(player1, List.of(new Ragamuffyn()));

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Regains double strike when its controller's hand becomes empty")
    void regainsDoubleStrikeWhenHandEmpties() {
        harness.setHand(player1, List.of(new Ragamuffyn()));
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setHand(player1, List.of());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Repeated pump activations stack and affect only their source")
    void repeatedPumpsAffectOnlyTheirSource() {
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        Permanent otherDragon = addCreatureReady(player1, new RakdosPitDragon());
        int basePower = gqs.getEffectivePower(gd, dragon);
        int otherPower = gqs.getEffectivePower(gd, otherDragon);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectivePower(gd, otherDragon)).isEqualTo(otherPower);
    }

    @Test
    @DisplayName("A summoning-sick dragon can activate both abilities without tapping")
    void summoningSickDragonCanActivateBothAbilities() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new RakdosPitDragon());
        Permanent otherDragon = addCreatureReady(player1, new RakdosPitDragon());
        int basePower = gqs.getEffectivePower(gd, dragon);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherDragon, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower + 1);
        assertThat(dragon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Hellbent uses each dragon's controller's hand independently")
    void hellbentUsesEachControllersHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Ragamuffyn()));
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        Permanent opposingDragon = addCreatureReady(player2, new RakdosPitDragon());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingDragon, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setHand(player1, List.of(new Ragamuffyn()));
        harness.setHand(player2, List.of());

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingDragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An unblocked hellbent dragon deals damage in both combat damage steps")
    void hellbentDragonDealsDoubleCombatDamage() {
        harness.setHand(player1, List.of());
        Permanent dragon = addCreatureReady(player1, new RakdosPitDragon());
        int power = gqs.getEffectivePower(gd, dragon);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, lifeBefore - 2 * power);
    }
}

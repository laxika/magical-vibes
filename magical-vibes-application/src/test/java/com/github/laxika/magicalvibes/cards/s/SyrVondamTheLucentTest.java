package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({SyrVondamTheLucent.class, GrizzlyBears.class, Unsummon.class})
class SyrVondamTheLucentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other creatures you control and gives them deathtouch")
    void entersAndBoostsOtherControlledCreatures() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Attacking boosts other creatures you control and gives them deathtouch")
    void attacksAndBoostsOtherControlledCreatures() {
        Permanent source = addCreatureReady(player1, new SyrVondamTheLucent());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and deathtouch grant wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive both bonuses")
    void includesCreaturesPresentAtResolution() {
        harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        Permanent other = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive either bonus")
    void excludesCreaturesEnteringAfterResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();

        Permanent late = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, existing, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, late)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, late, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The entry trigger resolves even if Syr Vondam leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Syr Vondam, the Lucent");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The entry and attack bonuses stack during the same turn")
    void entryAndAttackBonusesStack() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new SyrVondamTheLucent());
        resolveAllTriggers();
        source.setSummoningSick(false);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isTrue();
    }
}

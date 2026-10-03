package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CraterhoofBehemoth.class, NettleSwine.class})
@DisplayName("Craterhoof Behemoth")
class CraterhoofBehemothTest extends BaseCardTest {

    private void castBehemoth() {
        harness.castFromHand(player1, new CraterhoofBehemoth(), "{5}{G}{G}{G}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB pumps own creatures by the number of creatures you control and grants trample")
    void etbPumpsAndGrantsTrample() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        Permanent otherSwine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());

        castBehemoth();

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");

        // Three creatures on the battlefield when the trigger resolves, Craterhoof included.
        assertThat(swine.getPowerModifier()).isEqualTo(3);
        assertThat(swine.getToughnessModifier()).isEqualTo(3);
        assertThat(otherSwine.getPowerModifier()).isEqualTo(3);
        assertThat(hoof.getPowerModifier()).isEqualTo(3);
        assertThat(hoof.getToughnessModifier()).isEqualTo(3);

        assertThat(swine.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(hoof.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Alone on the battlefield it still pumps itself by one")
    void pumpsItselfWhenAlone() {
        castBehemoth();

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");

        assertThat(hoof.getPowerModifier()).isEqualTo(1);
        assertThat(hoof.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect creatures an opponent controls")
    void doesNotAffectOpponentCreatures() {
        Permanent opponentSwine = harness.addToBattlefieldAndReturn(player2, new NettleSwine());

        castBehemoth();

        assertThat(opponentSwine.getPowerModifier()).isEqualTo(0);
        assertThat(opponentSwine.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Pump and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());

        castBehemoth();

        assertThat(swine.getPowerModifier()).isEqualTo(2);
        assertThat(swine.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(swine.getPowerModifier()).isEqualTo(0);
        assertThat(swine.getToughnessModifier()).isEqualTo(0);
        assertThat(swine.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution are counted and receive the bonus")
    void countsCreaturesAtResolution() {
        harness.castFromHand(player1, new CraterhoofBehemoth(), "{5}{G}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent swine = harness.enterBattlefieldAndReturn(player1, new NettleSwine());
        harness.passBothPriorities();

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");
        assertThat(swine.getPowerModifier()).isEqualTo(2);
        assertThat(swine.getToughnessModifier()).isEqualTo(2);
        assertThat(swine.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(hoof.getPowerModifier()).isEqualTo(2);
        assertThat(hoof.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger resolves after its source dies and does not count the departed source")
    void resolvesAfterSourceLeaves() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        harness.castFromHand(player1, new CraterhoofBehemoth(), "{5}{G}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");
        hoof.setMarkedDamage(5);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hoof);
        harness.passBothPriorities();

        assertThat(swine.getPowerModifier()).isEqualTo(1);
        assertThat(swine.getToughnessModifier()).isEqualTo(1);
        assertThat(swine.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The bonus is fixed and creatures entering later receive neither effect")
    void doesNotRecalculateOrAffectLaterCreatures() {
        Permanent swine = harness.addToBattlefieldAndReturn(player1, new NettleSwine());
        castBehemoth();

        Permanent lateSwine = harness.enterBattlefieldAndReturn(player1, new NettleSwine());
        Permanent hoof = findPermanent(player1, "Craterhoof Behemoth");
        assertThat(lateSwine.getPowerModifier()).isZero();
        assertThat(lateSwine.getToughnessModifier()).isZero();
        assertThat(lateSwine.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(hoof.getPowerModifier()).isEqualTo(2);

        swine.setMarkedDamage(5);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(swine);
        assertThat(hoof.getPowerModifier()).isEqualTo(2);
        assertThat(hoof.getToughnessModifier()).isEqualTo(2);
        assertThat(hoof.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}

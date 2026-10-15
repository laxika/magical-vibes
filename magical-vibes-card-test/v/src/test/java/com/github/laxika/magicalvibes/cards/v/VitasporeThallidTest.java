package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitasporeThallid.class, SerraSphinx.class, GaeasAnthem.class})
class VitasporeThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerOnlyFiresDuringControllerUpkeep() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player2);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isOne();
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, saproling)).isOne();
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isOne();
    }

    @Test
    @DisplayName("Sacrificing a Saproling gives a target creature haste until end of turn")
    void sacrificingSaprolingGivesTargetCreatureHaste() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        Permanent target = addCreatureReady(player2, new SerraSphinx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addThallid().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The haste ability requires a Saproling and a creature target")
    void hasteAbilityRequiresSaprolingAndCreatureTarget() {
        Permanent thallid = addThallid();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new GaeasAnthem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("A Saproling source can be sacrificed when the cost does not say another")
    void canSacrificeSourceWhenItIsSaproling() {
        Permanent thallid = addThallid();
        thallid.getTransientSubtypes().add(CardSubtype.SAPROLING);
        Permanent target = addCreatureReady(player2, new SerraSphinx());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vitaspore Thallid");
        harness.assertInGraveyard(player1, "Vitaspore Thallid");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Spore counters are paid before the token ability resolves, even while summoning sick")
    void countersArePaidImmediatelyWhileSummoningSick() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new VitasporeThallid());
        thallid.setSummoningSick(true);
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the haste ability's cost")
    void cannotSacrificeOpponentsSaproling() {
        Permanent thallid = addThallid();
        Permanent opponentThallid = addCreatureReady(player2, new VitasporeThallid());
        opponentThallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, thallid.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(gqs.hasKeyword(gd, thallid, Keyword.HASTE)).isFalse();
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new VitasporeThallid());
    }
}

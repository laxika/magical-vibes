package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethSunsNemesis.class, GrizzlyBears.class})
class ElspethSunsNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("-1 boosts up to two creatures you control until end of turn")
    void minusOneBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent elspeth = addReadyElspeth(4);
        Permanent firstBear = addReadyBear(player1);
        Permanent secondBear = addReadyBear(player1);
        Permanent opposingBear = addReadyBear(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 creates two Human Soldier tokens")
    void minusTwoCreatesHumanSoldiers() {
        Permanent elspeth = addReadyElspeth(4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 gains five life")
    void minusThreeGainsFiveLife() {
        Permanent elspeth = addReadyElspeth(4);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Escape exiles four other graveyard cards and resolves onto the battlefield")
    void escapeExilesFourOtherCards() {
        ElspethSunsNemesis elspeth = new ElspethSunsNemesis();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(elspeth, first, second, third, fourth));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard()).isSameAs(elspeth));
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent permanent = new Permanent(new ElspethSunsNemesis());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyBear(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

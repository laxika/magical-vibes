package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.g.Grimdancer;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRevoker;
import com.github.laxika.magicalvibes.cards.s.SuddenSpinnerets;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnbreakableBond.class, AlmightyBrushwagg.class, SuddenSpinnerets.class,
        Grimdancer.class, PhyrexianRevoker.class})
class UnbreakableBondTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from your graveyard with a lifelink counter")
    void returnsCreatureWithLifelinkCounter() {
        Card creature = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(creature));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Almighty Brushwagg");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getId());
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreature() {
        Card instant = new SuddenSpinnerets();
        harness.setGraveyard(player1, List.of(instant));
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new AlmightyBrushwagg();
        harness.setGraveyard(player2, List.of(creature));
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedCreatureGainsLifeWhenItDealsCombatDamage() {
        Card creature = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 10);
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        Permanent returned = findPermanent(player1, "Almighty Brushwagg");
        returned.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new AlmightyBrushwagg();
        Card other = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(target, other));
        prepareCast();

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(target);
        harness.assertInGraveyard(player1, "Unbreakable Bond");
    }

    @Test
    void addsLifelinkCounterInAdditionToCreaturesOwnEntryCounters() {
        Card creature = new Grimdancer();
        harness.setGraveyard(player1, List.of(creature));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.handleListChoice(player1, "menace");
        harness.handleListChoice(player1, "lifelink");

        Permanent returned = findPermanent(player1, "Grimdancer");
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(2);
    }

    @Test
    void preservesLifelinkCounterAcrossAsEntersNameChoice() {
        Card creature = new PhyrexianRevoker();
        harness.setGraveyard(player1, List.of(creature));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.handleListChoice(player1, "Almighty Brushwagg");

        Permanent returned = findPermanent(player1, "Phyrexian Revoker");
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Phyrexian Revoker");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new UnbreakableBond()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

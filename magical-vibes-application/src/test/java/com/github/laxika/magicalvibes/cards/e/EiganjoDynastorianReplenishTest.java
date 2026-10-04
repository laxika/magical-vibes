package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EiganjoDynastorianReplenish.class, Replenish.class, GhostlyPrison.class, SolemnSimulacrum.class})
class EiganjoDynastorianReplenishTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when you attack with two creatures")
    void becomesPreparedWithTwoAttackers() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(dynastorian.isPrepared()).isTrue();
        assertThat(dynastorian.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when you attack with one creature")
    void doesNotBecomePreparedWithOneAttacker() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(1));

        assertThat(dynastorian.isPrepared()).isFalse();
        assertThat(dynastorian.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting Replenish returns all enchantment cards and leaves creatures in the graveyard")
    void replenishReturnsEnchantmentsOnly() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());
        addCreatureReady(player1, new SolemnSimulacrum());
        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        UUID copyId = dynastorian.getPreparedSpellCardId();
        Card enchantment = new GhostlyPrison();
        Card secondEnchantment = new GhostlyPrison();
        Card opponentEnchantment = new GhostlyPrison();
        Card creature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(enchantment, secondEnchantment, creature));
        harness.setGraveyard(player2, List.of(opponentEnchantment));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, copyId);
        assertThat(dynastorian.isPrepared()).isFalse();
        assertThat(dynastorian.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == secondEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentEnchantment);
        assertThat(dynastorian.isPrepared()).isFalse();
    }

    @Test
    void countsDynastorianAmongAttackersWithoutTappingIt() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());

        declareAttackers(List.of(0, 1));
        assertThat(dynastorian.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(dynastorian.isPrepared()).isTrue();
    }

    @Test
    void opponentsAttackDoesNotPrepareDynastorian() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player2, new SolemnSimulacrum());
        addCreatureReady(player2, new SolemnSimulacrum());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(dynastorian.isPrepared()).isFalse();
        assertThat(dynastorian.getPreparedSpellCardId()).isNull();
    }

    @Test
    void anotherAttackWhilePreparedDoesNotCreateAnotherCopy() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        UUID copyId = dynastorian.getPreparedSpellCardId();
        int exileCount = gd.exiledCards.size();

        harness.performUntapStep(player1);
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(dynastorian.isPrepared()).isTrue();
        assertThat(dynastorian.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exiledCards).hasSize(exileCount);
    }

    @Test
    void preparedReplenishCannotBeCastDuringCombat() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new SolemnSimulacrum());
        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        UUID copyId = dynastorian.getPreparedSpellCardId();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dynastorian.isPrepared()).isTrue();
        assertThat(dynastorian.getPreparedSpellCardId()).isEqualTo(copyId);
    }

}

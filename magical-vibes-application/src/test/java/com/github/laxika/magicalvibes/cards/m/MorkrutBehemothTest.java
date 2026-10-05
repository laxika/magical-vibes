package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorkrutBehemoth.class, GrizzlyBears.class})
class MorkrutBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Morkrut Behemoth");
    }

    @Test
    @DisplayName("Pays {1}{B} instead of sacrificing")
    void paysManaInsteadOfSacrificing() {
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Morkrut Behemoth");
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the additional cost")
    void cannotCastWithoutCreatureOrMana() {
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndDoesNotChargeExtraMana() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MorkrutBehemoth());
        sacrifice.setTapped(true);
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrifice.getId());

        harness.assertInGraveyard(player1, "Morkrut Behemoth");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Morkrut Behemoth");
    }

    @Test
    void canPayManaWhileKeepingAvailableCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MorkrutBehemoth());
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MorkrutBehemoth());
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    void manaOptionRequiresSecondBlackMana() {
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(7);
    }

    @Test
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new MorkrutBehemoth());
        addCreatureReady(player2, new MorkrutBehemoth());
        addCreatureReady(player2, new MorkrutBehemoth());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}

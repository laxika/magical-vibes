package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplenishTest extends BaseCardTest {

    private void castReplenish() {
        harness.setHand(player1, new ArrayList<>(List.of(new Replenish())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns all non-Aura enchantment cards from your graveyard to the battlefield")
    void returnsAllEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(enchantment, creature));

        castReplenish();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Leaves Auras with nothing to enchant in the graveyard")
    void leavesOrphanedAurasInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card aura = new ArmorOfFaith();
        gd.playerGraveyards.get(player1.getId()).add(aura);

        castReplenish();

        harness.assertNotOnBattlefield(player1, "Armor of Faith");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Does not return enchantments from an opponent's graveyard")
    void doesNotReturnOpponentsEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentEnchantment = new GloriousAnthem();
        gd.playerGraveyards.get(player2.getId()).add(opponentEnchantment);

        castReplenish();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentEnchantment);
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
    }
}

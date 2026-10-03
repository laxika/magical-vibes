package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanaTheBloodsower.class, SuntailHawk.class})
class TanaTheBloodsowerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates that many 1/1 green Saproling tokens")
    void combatDamageCreatesSaprolingsEqualToDamageDealt() {
        Permanent tana = addCreatureReady(player1, new TanaTheBloodsower());
        tana.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Trample damage creates Saprolings equal to damage dealt to the player")
    void trampleDamageCreatesSaprolingsEqualToPlayerDamage() {
        Permanent tana = addCreatureReady(player1, new TanaTheBloodsower());
        tana.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("No Saprolings are created when no combat damage reaches a player")
    void noTokensWithoutCombatDamageToPlayer() {
        Permanent tana = addCreatureReady(player1, new TanaTheBloodsower());
        tana.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TanaTheBloodsower());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }
}

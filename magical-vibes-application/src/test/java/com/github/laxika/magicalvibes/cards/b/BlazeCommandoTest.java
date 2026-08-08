package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlazeCommandoTest extends BaseCardTest {

    private long soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("An instant you control dealing damage creates two hasty Soldier tokens")
    void instantDamageCreatesTwoSoldiers() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isEqualTo(2);
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();
        assertThat(soldier.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("One spell damaging several creatures at once triggers only once")
    void simultaneousDamageTriggersOnce() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's damage spell does not trigger it")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isZero();
        assertThat(soldierTokens(player2)).isZero();
    }

    @Test
    @DisplayName("Combat damage from a creature does not trigger it")
    void combatDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlazeCommando());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(soldierTokens(player1)).isZero();
    }
}

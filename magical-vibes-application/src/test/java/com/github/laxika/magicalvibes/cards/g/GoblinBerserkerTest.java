package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WildColos;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBerserker.class, WildColos.class})
class GoblinBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        harness.setHand(player1, List.of(new GoblinBerserker()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent berserker = findPermanent(player1, "Goblin Berserker");
        assertThat(berserker.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("First strike kills an equally sized blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent berserker = addCreatureReady(player1, new GoblinBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, new WildColos());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Goblin Berserker");
        harness.assertInGraveyard(player2, "Wild Colos");
    }
}

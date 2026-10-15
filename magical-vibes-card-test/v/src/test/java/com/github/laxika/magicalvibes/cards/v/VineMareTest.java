package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.p.PoisonTipArcher;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VineMare.class, WalkingCorpse.class, CentaurCourser.class, Shock.class, PoisonTipArcher.class})
class VineMareTest extends BaseCardTest {

    @Test
    @DisplayName("Vine Mare can't be blocked by black creatures")
    void cannotBeBlockedByBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new VineMare());
        addCreatureReady(player2, new WalkingCorpse());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vine Mare can't be blocked by creatures that are black and another color")
    void cannotBeBlockedByMulticoloredBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new VineMare());
        addCreatureReady(player2, new PoisonTipArcher());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vine Mare can be blocked by nonblack creatures")
    void canBeBlockedByNonblackCreature() {
        Permanent attacker = addCreatureReady(player1, new VineMare());
        Permanent blocker = addCreatureReady(player2, new CentaurCourser());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target Vine Mare with spells")
    void opponentCannotTargetWithSpells() {
        Permanent vineMare = addCreatureReady(player1, new VineMare());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, vineMare.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Vine Mare with spells")
    void controllerCanTargetWithSpells() {
        Permanent vineMare = addCreatureReady(player1, new VineMare());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, vineMare.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vineMare);
        assertThat(vineMare.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Shock);
    }
}

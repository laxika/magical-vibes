package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MimingSlimeTest extends BaseCardTest {

    private Optional<Permanent> ooze(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Ooze"))
                .findFirst();
    }

    @Test
    @DisplayName("Ooze token is X/X where X is the greatest power among your creatures")
    void oozeMatchesGreatestPower() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = ooze(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures your opponent controls do not count")
    void ignoresOpponentCreatures() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = ooze(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creatures the 0/0 Ooze dies immediately")
    void zeroSizedOozeDies() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ooze(player1)).isEmpty();
    }
}

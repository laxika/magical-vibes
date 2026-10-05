package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MimingSlime.class, GrizzlyBears.class, HillGiant.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = ooze(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("With no creatures the 0/0 Ooze dies immediately")
    void zeroSizedOozeDies() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ooze(player1)).isEmpty();
    }

    @Test
    @DisplayName("Token size uses effective power at resolution and remains fixed afterward")
    void effectivePowerIsDeterminedAtResolution() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        Permanent token = ooze(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("A creature that leaves before resolution does not determine token size")
    void ignoresCreatureThatLeftBeforeResolution() {
        harness.setHand(player1, List.of(new MimingSlime()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castSorcery(player1, 0, 0);
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        gd.playerGraveyards.get(player1.getId()).add(giant.getCard());
        harness.passBothPriorities();

        Permanent token = ooze(player1).orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }
}

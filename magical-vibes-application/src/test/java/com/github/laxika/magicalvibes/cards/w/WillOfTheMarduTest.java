package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheMardu.class, EdgarMarkov.class, GrizzlyBears.class, HillGiant.class})
class WillOfTheMarduTest extends BaseCardTest {

    @Test
    void createsWarriorsForEachCreatureTargetPlayerControls() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castSingleMode(0, player2.getId());

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
        assertThat(countPermanents(player2, "Warrior")).isZero();
    }

    @Test
    void dealsDamageEqualToCreaturesYouControl() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        castSingleMode(1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void commanderAllowsBothModes() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotChooseBothModesWithoutACommander() {
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}

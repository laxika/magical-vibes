package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheAbzan.class, EdgarMarkov.class, GrizzlyBears.class, HillGiant.class})
class WillOfTheAbzanTest extends BaseCardTest {

    @Test
    void sacrificesOnlyTheGreatestPowerCreatureAndLosesLife() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        castSingleMode(0, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void targetPlayerChoosesAmongTiedGreatestPowerCreaturesBeforeLifeLoss() {
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
        harness.handlePermanentChosen(player2, first.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void returnsTargetCreatureFromOwnGraveyardToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castSingleMode(1, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void commanderAllowsBothModes() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player2, new HillGiant());
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());

        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotChooseBothModesWithoutCommander() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OlRinsSearingLight.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class OlRinsSearingLightTest extends BaseCardTest {

    @Test
    void exilesOnlyTheGreatestPowerCreatureWithoutSpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castSearingLight();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(angel.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void spellMasteryDealsDamageEqualToTheExiledCreaturePower() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        castSearingLight();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(angel.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void opponentChoosesAmongTiedGreatestPowerCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        castSearingLight();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void castSearingLight() {
        harness.setHand(player1, List.of(new OlRinsSearingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}

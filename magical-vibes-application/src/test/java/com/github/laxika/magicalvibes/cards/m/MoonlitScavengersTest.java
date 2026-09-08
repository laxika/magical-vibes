package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonlitScavengers.class, GrizzlyBears.class, Spellbook.class, MysticRemora.class})
class MoonlitScavengersTest extends BaseCardTest {

    @Test
    void returnsOpponentCreatureWhenControllingArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");

        castMoonlitScavengers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreatureId);
        assertThat(choice.validIds()).doesNotContain(ownCreatureId);

        harness.handlePermanentChosen(player1, opponentCreatureId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Moonlit Scavengers");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnsOpponentCreatureWhenControllingEnchantment() {
        harness.addToBattlefield(player1, new MysticRemora());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMoonlitScavengers();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerWithoutArtifactOrEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMoonlitScavengers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNothingIfGateIsLostBeforeResolution() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMoonlitScavengers();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Spellbook"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castMoonlitScavengers() {
        harness.setHand(player1, List.of(new MoonlitScavengers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

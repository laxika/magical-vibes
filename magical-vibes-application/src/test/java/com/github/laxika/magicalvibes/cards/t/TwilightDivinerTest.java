package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwilightDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a surveil 2 interaction")
    void surveilsTwoOnEnter() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwilightDiviner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("A creature entering from a graveyard creates one token copy")
    void reanimatedCreatureCreatesTokenCopy() {
        Card firstCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(firstCreature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, firstCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The graveyard trigger does not fire for an ordinary creature or twice in one turn")
    void onlyTriggersOnceForGraveyardOrigin() {
        Card ordinaryCreature = new GrizzlyBears();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setHand(player1, List.of(ordinaryCreature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new Zombify(), new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castSorcery(player1, 0, firstCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cast from a graveyard creates a token copy")
    void castFromGraveyardCreatesTokenCopy() {
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Worldheart Phoenix")).isEqualTo(2);
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExquisiteFirecraft.class, Cancel.class, GrizzlyBears.class, LightningBolt.class})
class ExquisiteFirecraftTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player")
    void dealsFourDamageToPlayer() {
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature, destroying it")
    void dealsFourDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without spell mastery it can be countered")
    void withoutSpellMasteryCanBeCountered() {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firecraft.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Exquisite Firecraft");
    }

    @Test
    @DisplayName("Spell mastery — with two instants in the graveyard it can't be countered")
    void withSpellMasteryCannotBeCountered() {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setGraveyard(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firecraft.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void mixedInstantAndSorceryEnableSpellMastery() {
        attemptCounter(List.of(new LightningBolt(), new ExquisiteFirecraft()), List.of());
        harness.assertLife(player2, 16);
    }

    @Test
    void twoSorceriesEnableSpellMastery() {
        attemptCounter(List.of(new ExquisiteFirecraft(), new ExquisiteFirecraft()), List.of());
        harness.assertLife(player2, 16);
    }

    @Test
    void creatureCardsDoNotCountForSpellMastery() {
        attemptCounter(List.of(new LightningBolt(), new GrizzlyBears()), List.of());
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsGraveyardDoesNotEnableSpellMastery() {
        attemptCounter(List.of(), List.of(new LightningBolt(), new LightningBolt()));
        harness.assertLife(player2, 20);
    }

    @Test
    void instantResolvingInResponseEnablesSpellMastery() {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft, new LightningBolt()));
        harness.setGraveyard(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firecraft.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void attemptCounter(List<Card> controllerGraveyard, List<Card> opponentGraveyard) {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft));
        harness.setGraveyard(player1, controllerGraveyard);
        harness.setGraveyard(player2, opponentGraveyard);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firecraft.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }
}

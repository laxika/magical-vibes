package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheGuildpact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Quench.class, LlanowarElves.class, GrizzlyBears.class, Island.class,
        RhythmOfTheWild.class, SphinxOfTheGuildpact.class})
class QuenchTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellResolvesWhenControllerPaysTwo() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void fizzlesIfTargetSpellLeavesTheStack() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(stackEntry -> stackEntry.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Quench");
    }

    @Test
    void canGeneratePaymentManaDuringResolution() {
        SphinxOfTheGuildpact sphinx = new SphinxOfTheGuildpact();
        harness.castFromHand(player1, sphinx, "{7}");
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sphinx of the Guildpact");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void mayPayEvenWhenSpellCannotBeCountered() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        SphinxOfTheGuildpact sphinx = new SphinxOfTheGuildpact();
        harness.castFromHand(player1, sphinx, "{7}");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Sphinx of the Guildpact");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(sphinx.getId()));
    }

    @Test
    void controllerMayDeclinePaymentDespiteHavingEnoughMana() {
        SphinxOfTheGuildpact sphinx = new SphinxOfTheGuildpact();
        harness.castFromHand(player1, sphinx, "{7}");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Quench()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Sphinx of the Guildpact");
        harness.assertNotOnBattlefield(player1, "Sphinx of the Guildpact");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}

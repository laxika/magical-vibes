package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhostLitWarderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityCountersSpellWhenControllerCannotPay() {
        Permanent warder = addCreatureReady(player1, new GhostLitWarder());

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(warder.isTapped()).isTrue();
    }

    @Test
    void battlefieldAbilityLetsSpellResolveWhenControllerPays() {
        Permanent warder = addCreatureReady(player1, new GhostLitWarder());

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(warder.isTapped()).isTrue();
    }

    @Test
    void channelCountersSpellWhenControllerCannotPay() {
        harness.setHand(player1, List.of(new GhostLitWarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Warder");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void channelLetsSpellResolveWhenControllerPays() {
        harness.setHand(player1, List.of(new GhostLitWarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateHandAbility(player1, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Warder");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}

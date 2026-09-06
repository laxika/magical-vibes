package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForceSpike.class, LlanowarElves.class, DarkRitual.class})
class ForceSpikeTest extends BaseCardTest {
    @Test
    @DisplayName("Counters spell when opponent has no mana to pay {1}")
    void countersWhenOpponentCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1); // just enough to cast

        harness.setHand(player2, List.of(new ForceSpike()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a noncreature spell when its controller cannot pay {1}")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setHand(player2, List.of(new ForceSpike()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ritual.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Ritual");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Spell is not countered when opponent pays {1}")
    void spellNotCounteredWhenOpponentPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 to pay

        harness.setHand(player2, List.of(new ForceSpike()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.setHand(player2, List.of(new ForceSpike()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }
}

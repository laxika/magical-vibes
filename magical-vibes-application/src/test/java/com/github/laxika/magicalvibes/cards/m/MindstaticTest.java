package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindstatic.class, KraulWarrior.class})
class MindstaticTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay {6}")
    void countersWhenOpponentCannotPay() {
        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Mindstatic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Kraul Warrior");
        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {6}")
    void spellNotCounteredWhenOpponentPays() {
        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new Mindstatic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Kraul Warrior");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kraul Warrior");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay {6}")
    void spellCounteredWhenOpponentDeclines() {
        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.setHand(player2, List.of(new Mindstatic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kraul Warrior");
        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
    }

    @Test
    @DisplayName("Five available mana is insufficient and is not spent")
    void countersWhenOneManaShort() {
        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.setHand(player2, List.of(new Mindstatic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        harness.assertInGraveyard(player1, "Kraul Warrior");
        harness.assertInGraveyard(player2, "Mindstatic");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic payment accepts mixed colors and spends exactly six mana")
    void paysWithMixedColorsAndKeepsExcessMana() {
        KraulWarrior warrior = new KraulWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Mindstatic()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Kraul Warrior");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kraul Warrior");
        harness.assertInGraveyard(player2, "Mindstatic");
    }

    @Test
    @DisplayName("Counters a noncreature spell and preserves its original target")
    void countersAnotherMindstatic() {
        KraulWarrior warrior = new KraulWarrior();
        Mindstatic opposingCounter = new Mindstatic();
        harness.setHand(player1, List.of(warrior, new Mindstatic()));
        harness.setHand(player2, List.of(opposingCounter));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingCounter.getId());

        harness.assertInGraveyard(player2, "Mindstatic");
        harness.assertInGraveyard(player1, "Mindstatic");
        harness.assertNotInGraveyard(player1, "Kraul Warrior");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kraul Warrior");
    }
}

package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BackupAgent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DigUpTheBody.class, Forest.class, BackupAgent.class})
class DigUpTheBodyTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards, then may return any creature card from the graveyard")
    void millsThenReturnsCreatureFromGraveyard() {
        Card creatureAlreadyInGraveyard = new BackupAgent();
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card spell = new DigUpTheBody();
        harness.setGraveyard(player1, List.of(creatureAlreadyInGraveyard));
        harness.setLibrary(player1, List.of(milledOne, milledTwo));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                creatureAlreadyInGraveyard, milledOne, milledTwo);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureAlreadyInGraveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, spell);
    }

    @Test
    @DisplayName("Declining the return still mills two cards")
    void decliningReturnStillMills() {
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(milledOne, milledTwo));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, spell);
    }

    @Test
    @DisplayName("Casualty copies the spell and sacrifices the chosen creature")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new BackupAgent());
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card milledThree = new Forest();
        Card milledFour = new Forest();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree, milledFour));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithSacrifice(player1, 0, null, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                casualtyCreature.getCard(), milledOne, milledTwo, milledThree, milledFour, spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Can return a creature just milled, but not a land or an opponent's creature")
    void choosesCreatureAfterMilling() {
        Card land = new Forest();
        Card creature = new BackupAgent();
        Card opponentsCreature = new BackupAgent();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(land, creature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.GraveyardChoice.class,
                        choice -> assertThat(choice.validIndices()).containsExactly(1));
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
    }

    @Test
    @DisplayName("A one-card library mills its remaining card and still permits returning it")
    void millsShortLibraryAndReturnsCreature() {
        Card creature = new BackupAgent();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("An empty library does not prevent returning a creature already in the graveyard")
    void returnsCreatureWithEmptyLibrary() {
        Card creature = new BackupAgent();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("The copy may return the casualty creature while the original declines its return")
    void copyReturnsSacrificedCreatureAndOriginalDeclines() {
        Permanent casualtyCreature = addCreatureReady(player1, new BackupAgent());
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithSacrifice(player1, 0, null, casualtyCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(casualtyCreature.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casualtyCreature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third, fourth, spell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casualtyCreature.getCard());
    }

    @Test
    @DisplayName("Casualty rejects a creature whose current power is zero")
    void casualtyRejectsZeroPowerCreature() {
        Permanent creature = addCreatureReady(player1, new BackupAgent());
        creature.setPowerModifier(-1);
        Card spell = new DigUpTheBody();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}

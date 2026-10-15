package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.cards.p.PloverKnights;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuillSlingerBoggart.class, PloverKnights.class, OakgnarlWarrior.class, AvianChangeling.class})
class QuillSlingerBoggartTest extends BaseCardTest {

    private void giveKithkinSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new PloverKnights()));
        harness.addMana(caster, ManaColor.WHITE, 5);
    }

    @Test
    @DisplayName("Casting a Kithkin spell asks the controller for a target before the may choice")
    void kithkinSpellTriggers() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        giveKithkinSpell(player1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting makes the chosen target player lose 1 life")
    void acceptDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        giveKithkinSpell(player1);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Declining leaves all life totals unchanged")
    void declineLeavesLife() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        giveKithkinSpell(player1);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Casting a non-Kithkin spell does not trigger the ability")
    void nonKithkinDoesNotTrigger() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        harness.setHand(player1, List.of(new OakgnarlWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Any player casting a Kithkin spell triggers the controller's ability")
    void opponentKithkinTriggersController() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveKithkinSpell(player2);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The controller may target themselves and decide on resolution")
    void controllerCanLoseLife() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        giveKithkinSpell(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A changeling spell counts as Kithkin before it resolves")
    void changelingSpellTriggers() {
        harness.addToBattlefield(player1, new QuillSlingerBoggart());
        harness.setHand(player1, List.of(new AvianChangeling()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Avian Changeling");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Avian Changeling");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Avian Changeling");
    }
}

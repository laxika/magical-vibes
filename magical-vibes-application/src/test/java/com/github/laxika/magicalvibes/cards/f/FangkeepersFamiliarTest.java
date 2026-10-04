package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SealOfStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FangkeepersFamiliar.class, FaerieInvaders.class, GrizzlyBears.class, SealOfStrength.class})
class FangkeepersFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode gains 3 life and surveils 3")
    void lifeAndSurveilMode() {
        Card topCard = new GrizzlyBears();
        Card middleCard = new SealOfStrength();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, middleCard, bottomCard));
        harness.setLife(player1, 10);

        castFangkeepersFamiliar(0);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(middleCard, bottomCard);
    }

    @Test
    @DisplayName("ETB mode destroys target enchantment")
    void destroyEnchantmentMode() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfStrength());

        castFangkeepersFamiliar(1, enchantment.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SealOfStrength());

        enterAndChooseMode("Destroy target enchantment");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ETB mode counters a creature spell")
    void counterCreatureSpellMode() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new FangkeepersFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        FaerieInvaders creatureSpell = new FaerieInvaders();
        harness.castFromHand(player2, creatureSpell, "{4}{U}");

        harness.castCreature(player1, 0, 2, creatureSpell.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Faerie Invaders");
    }

    @Test
    void canChooseDestroyModeWhenEnteringWithoutBeingCast() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfStrength());

        enterAndChooseMode("Destroy target enchantment");
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Seal of Strength");
        harness.assertInGraveyard(player2, "Seal of Strength");
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(FangkeepersFamiliar.class)
    void gainLifeModeStillGainsLifeWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 10);

        castFangkeepersFamiliar(0);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Fangkeeper's Familiar");
    }

    @Test
    @CardUsed(FangkeepersFamiliar.class)
    void surveilCanKeepAndReorderAllCardsInAShortLibrary() {
        Card first = new FangkeepersFamiliar();
        Card second = new FangkeepersFamiliar();
        harness.setLibrary(player1, List.of(first, second));

        castFangkeepersFamiliar(0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 23);
    }

    private void enterAndChooseMode(String mode) {
        harness.enterBattlefieldAndReturn(player1, new FangkeepersFamiliar());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, mode);
    }

    private void castFangkeepersFamiliar(int mode) {
        castFangkeepersFamiliar(mode, null);
    }

    private void castFangkeepersFamiliar(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FangkeepersFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

}

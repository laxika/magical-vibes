package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifecraftersBestiaryTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's upkeep, Lifecrafter's Bestiary scries 1")
    void scriesAtControllerUpkeep() {
        Card top = new Forest();
        Card bottom = new GrizzlyBears();
        harness.addToBattlefield(player1, new LifecraftersBestiary());
        harness.setLibrary(player1, List.of(top, bottom));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(bottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Lifecrafter's Bestiary does not scry during an opponent's upkeep")
    void doesNotScryDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new LifecraftersBestiary());
        gd.turnNumber = 2;
        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Casting a creature lets its controller pay {G} to draw a card")
    void paysGreenToDrawAfterCastingCreature() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new LifecraftersBestiary());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining the creature-cast payment does not draw")
    void decliningCreatureCastPaymentDoesNotDraw() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new LifecraftersBestiary());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Lifecrafter's Bestiary")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LifecraftersBestiary());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Lifecrafter's Bestiary"));
    }
}

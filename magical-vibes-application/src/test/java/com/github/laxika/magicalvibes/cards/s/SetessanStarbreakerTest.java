package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SetessanStarbreakerTest extends BaseCardTest {

    private static Card aura(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        return card;
    }

    private Permanent addAura(Player controller) {
        Permanent host = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        Permanent aura = new Permanent(aura("Test Aura"));
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void castAndAcceptMay(UUID auraId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, auraId);
    }

    @Test
    @DisplayName("ETB prompts to destroy an Aura when one exists")
    void etbPromptsToDestroyAura() {
        addAura(player2);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the ETB destroys the target Aura")
    void acceptingEtbDestroysAura() {
        UUID auraId = addAura(player2).getId();
        castAndAcceptMay(auraId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Test Aura");
        harness.assertInGraveyard(player2, "Test Aura");
    }

    @Test
    @DisplayName("Declining the ETB leaves the Aura on the battlefield")
    void decliningEtbLeavesAura() {
        addAura(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Test Aura");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not prompt when no Aura exists")
    void etbDoesNotPromptWithoutAura() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SetessanStarbreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Setessan Starbreaker");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can destroy an Aura controlled by its controller")
    void etbCanDestroyOwnAura() {
        UUID auraId = addAura(player1).getId();
        castAndAcceptMay(auraId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Test Aura");
        harness.assertInGraveyard(player1, "Test Aura");
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.a.AncestralAnger;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepositorySkaab.class, SporebackWolf.class, Abrade.class, AncestralAnger.class})
class RepositorySkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not return a spell card")
    void decliningExploitDoesNothing() {
        Abrade instant = new Abrade();
        harness.setGraveyard(player1, List.of(instant));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Repository Skaab");
        harness.assertInGraveyard(player1, "Abrade");
    }

    @Test
    @DisplayName("Exploit sacrifices a creature and returns a target instant to hand")
    void exploitReturnsInstantToHand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        Abrade instant = new Abrade();
        harness.setGraveyard(player1, List.of(instant));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repository Skaab");
        harness.assertNotOnBattlefield(player1, "Sporeback Wolf");
        harness.assertInHand(player1, "Abrade");
    }

    @Test
    @DisplayName("Exploit can return a target sorcery to hand")
    void exploitReturnsSorceryToHand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        AncestralAnger sorcery = new AncestralAnger();
        harness.setGraveyard(player1, List.of(sorcery));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancestral Anger");
        harness.assertNotInGraveyard(player1, "Ancestral Anger");
    }

    @Test
    @DisplayName("Exploit trigger cannot target a creature card")
    void exploitCannotTargetCreatureCard() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        Card creature = new SporebackWolf();
        harness.setGraveyard(player1, List.of(creature));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player1, "Sporeback Wolf");
    }

    @Test
    @DisplayName("Sacrificing Repository Skaab to its own exploit still returns a spell")
    void selfSacrificeReturnsSpell() {
        Abrade instant = new Abrade();
        harness.setGraveyard(player1, List.of(instant));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Repository Skaab"));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Repository Skaab");
        harness.assertInGraveyard(player1, "Repository Skaab");
        harness.assertInHand(player1, "Abrade");
        harness.assertNotInGraveyard(player1, "Abrade");
    }

    @Test
    @DisplayName("Exploit targets only spell cards in the controller's graveyard")
    void targetsOnlyOwnGraveyardSpells() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        Abrade instant = new Abrade();
        AncestralAnger sorcery = new AncestralAnger();
        SporebackWolf creature = new SporebackWolf();
        Abrade opposingSpell = new Abrade();
        harness.setGraveyard(player1, List.of(instant, sorcery, creature));
        harness.setGraveyard(player2, List.of(opposingSpell));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingSpell.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancestral Anger");
        harness.assertInGraveyard(player1, "Abrade");
        harness.assertInGraveyard(player1, "Sporeback Wolf");
        harness.assertInGraveyard(player2, "Abrade");
    }

    @Test
    @DisplayName("The exploit bonus requires exactly one target when legal targets exist")
    void mustChooseExactlyOneSpell() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        Abrade instant = new Abrade();
        AncestralAnger sorcery = new AncestralAnger();
        harness.setGraveyard(player1, List.of(instant, sorcery));

        castRepositorySkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Abrade");
        harness.assertInGraveyard(player1, "Ancestral Anger");
    }

    @Test
    @DisplayName("Removing Skaab before exploit resolves prevents its return ability")
    void removedBeforeExploitDoesNotReturnSpell() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SporebackWolf());
        Abrade instant = new Abrade();
        harness.setGraveyard(player1, List.of(instant));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RepositorySkaab(), "{3}{U}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, 0, harness.getPermanentId(player1, "Repository Skaab"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Repository Skaab");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sporeback Wolf");
        harness.assertInGraveyard(player1, "Repository Skaab");
        harness.assertInGraveyard(player1, "Abrade");
        harness.assertNotInHand(player1, "Abrade");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castRepositorySkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RepositorySkaab(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

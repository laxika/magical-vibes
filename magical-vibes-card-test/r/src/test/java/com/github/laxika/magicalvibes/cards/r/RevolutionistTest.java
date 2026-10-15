package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Revolutionist.class, Shock.class, GrizzlyBears.class, RavensCrime.class})
class RevolutionistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target instant or sorcery card from the controller's graveyard")
    void returnsTargetInstantOrSorceryToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new GrizzlyBears()));
        harness.castFromHand(player1, new Revolutionist(), "{5}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not offer non-instant or sorcery cards or an opponent's graveyard cards")
    void excludesInvalidGraveyardCards() {
        Card opponentShock = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.castFromHand(player1, new Revolutionist(), "{5}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Madness casts Revolutionist for {3}{R} and its ETB returns a spell")
    void madnessCastTriggersEnterTheBattlefieldAbility() {
        Shock shock = new Shock();
        Revolutionist revolutionist = new Revolutionist();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(revolutionist));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(revolutionist.getId()));
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("ETB returns exactly one chosen sorcery and leaves other eligible cards in the graveyard")
    void returnsChosenSorceryOnly() {
        RavensCrime sorcery = new RavensCrime();
        Shock instant = new Shock();
        harness.setGraveyard(player1, List.of(sorcery, instant));
        harness.castFromHand(player1, new Revolutionist(), "{5}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(sorcery.getId(), instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Raven's Crime");
        harness.assertNotInGraveyard(player1, "Raven's Crime");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInHand(player1, "Shock");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Shock target = new Shock();
        RavensCrime other = new RavensCrime();
        harness.setGraveyard(player1, List.of(target, other));
        harness.castFromHand(player1, new Revolutionist(), "{5}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Shock");
        harness.assertNotInHand(player1, "Raven's Crime");
        harness.assertInGraveyard(player1, "Raven's Crime");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining madness puts Revolutionist into its owner's graveyard without an ETB")
    void decliningMadnessPutsCardIntoGraveyard() {
        Revolutionist revolutionist = new Revolutionist();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(revolutionist));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.findExiledCard(revolutionist.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Revolutionist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(revolutionist.getId())).isNull();
        harness.assertInGraveyard(player1, "Revolutionist");
        harness.assertNotOnBattlefield(player1, "Revolutionist");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInHand(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

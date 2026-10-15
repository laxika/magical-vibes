package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BeguilerOfWills;
import com.github.laxika.magicalvibes.cards.d.DrogskolCaptain;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Seance.class, SilverclawGriffin.class, DrogskolCaptain.class, GatherTheTownsfolk.class,
        BeguilerOfWills.class})
class SeanceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Triggers during controller's upkeep and prompts may ability")
    void triggersDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Triggers during opponent's upkeep for Séance controller")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may exiles creature from graveyard and creates Spirit token copy")
    void acceptingMayCreatesSpiritTokenCopy() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Silverclaw Griffin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Silverclaw Griffin"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Silverclaw Griffin"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GRIFFIN, CardSubtype.SPIRIT);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining may leaves graveyard unchanged and creates no token")
    void decliningMayDoesNothing() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Silverclaw Griffin");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Silverclaw Griffin") && p.getCard().isToken());

        advanceToEndStep();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Silverclaw Griffin") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Graveyard target is chosen before the upkeep trigger resolves")
    void choosesTargetWhenTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin(), new DrogskolCaptain()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("No creature in the controller's graveyard means no legal upkeep trigger")
    void cannotTargetNoncreaturesOrOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new GatherTheTownsfolk()));
        harness.setGraveyard(player2, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Gather the Townsfolk");
        harness.assertInGraveyard(player2, "Silverclaw Griffin");
    }

    @Test
    @DisplayName("Token copies retain every color of a multicolored creature")
    void copiesAllCreatureColorsAndAbilities() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new DrogskolCaptain()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Drogskol Captain");
        assertThat(gqs.getEffectiveColors(gd, token))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("The token copy retains the creature's static ability")
    void copiedCaptainBuffsOtherSpirits() {
        harness.addToBattlefield(player1, new Seance());
        harness.addToBattlefield(player1, new DrogskolCaptain());
        harness.setGraveyard(player1, List.of(new DrogskolCaptain()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Drogskol Captain")).hasSize(2).allSatisfy(captain -> {
            assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, captain, Keyword.HEXPROOF)).isTrue();
        });
    }

    @Test
    @DisplayName("Token created on opponent's upkeep is controlled by Seance's controller")
    void createsTokenOnOpponentsTurnAndExilesAtThatEndStep() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new SilverclawGriffin()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Silverclaw Griffin");
        harness.assertNotOnBattlefield(player2, "Silverclaw Griffin");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Silverclaw Griffin");
        harness.assertNotInGraveyard(player1, "Silverclaw Griffin");
    }

    @Test
    @DisplayName("Changing token control does not change the delayed trigger's controller or source")
    void delayedExileRetainsOriginalControllerAndSource() {
        harness.addToBattlefield(player1, new Seance());
        harness.setGraveyard(player1, List.of(new DrogskolCaptain()));
        addCreatureReady(player2, new BeguilerOfWills());
        harness.addToBattlefield(player2, new SilverclawGriffin());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Drogskol Captain");
        harness.activateAbility(player2, 0, null, token.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Drogskol Captain");

        advanceToEndStep();

        assertThat(gd.stack).singleElement().satisfies(trigger -> {
            assertThat(trigger.getControllerId()).isEqualTo(player1.getId());
            assertThat(trigger.getCard().getName()).isEqualTo("Séance");
        });
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Drogskol Captain");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}

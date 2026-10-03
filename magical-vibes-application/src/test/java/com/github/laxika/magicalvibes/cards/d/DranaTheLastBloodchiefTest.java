package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CanyonJerboa;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DranaTheLastBloodchief.class, CanyonJerboa.class, Island.class, JaceMirrorMage.class})
class DranaTheLastBloodchiefTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player chooses a nonlegendary creature to return with a counter and Vampire subtype")
    void defendingPlayerChoosesCreatureToReturn() {
        Card firstCreature = new CanyonJerboa();
        Card chosenCreature = new CanyonJerboa();
        Card land = new Island();
        Card legendaryCreature = new DranaTheLastBloodchief();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(firstCreature, chosenCreature, land, legendaryCreature));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drana)));
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(firstCreature, chosenCreature);

        harness.handleGraveyardCardChosen(player2, choice.cardPool().indexOf(chosenCreature));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chosenCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.VAMPIRE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.MOUSE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCreature, land, legendaryCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger ignores noncreature and legendary creature cards")
    void ignoresIneligibleGraveyardCards() {
        Card land = new Island();
        Card legendaryCreature = new DranaTheLastBloodchief();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(land, legendaryCreature));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(drana)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, legendaryCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legendaryCreature.getId()));
    }

    @Test
    void returnsOnlyEligibleCreatureWithoutPromptAndKeepsVampireAfterDranaLeaves() {
        Card creature = new CanyonJerboa();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Canyon Jerboa");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isAttacking()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, drana));

        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.VAMPIRE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.MOUSE)).isTrue();
    }

    @Test
    void choosesFromGraveyardAtResolutionEvenAfterDranaLeaves() {
        Card firstCreature = new CanyonJerboa();
        Card newlyAddedCreature = new CanyonJerboa();
        Permanent drana = addCreatureReady(player1, new DranaTheLastBloodchief());
        harness.setGraveyard(player1, List.of(firstCreature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, drana));
        harness.setGraveyard(player1, List.of(firstCreature, newlyAddedCreature));
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(firstCreature, newlyAddedCreature);
        harness.handleGraveyardCardChosen(player2, 1);

        Permanent returned = findPermanent(player1, "Canyon Jerboa");
        assertThat(returned.getCard().getId()).isEqualTo(newlyAddedCreature.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.VAMPIRE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCreature);
    }

    @Test
    void stillReturnsCreatureWhenAttackedPlaneswalkerLeavesBeforeResolution() {
        Card creature = new CanyonJerboa();
        addCreatureReady(player1, new DranaTheLastBloodchief());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setGraveyard(player1, List.of(creature));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId())));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, planeswalker));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canyon Jerboa");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Canyon Jerboa").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }
}

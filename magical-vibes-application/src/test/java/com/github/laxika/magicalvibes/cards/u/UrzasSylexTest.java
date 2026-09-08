package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UrzasSylexTest extends BaseCardTest {

    @Test
    @DisplayName("The exile trigger resolves before each player chooses lands and destroys the rest")
    void searchResolvesBeforeLandChoicesAndBoardWipe() {
        UrzasSylex sylex = new UrzasSylex();
        harness.addToBattlefield(player1, sylex);
        List<Permanent> player1Lands = addPlains(player1, 7);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Permanent> player2Lands = addPlains(player2, 7);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        JaceBeleren jace = new JaceBeleren();
        harness.setLibrary(player1, List.of(jace, new GrizzlyBears()));

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(jace);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex);
        assertThat(gd.playerHands.get(player1.getId())).contains(jace);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature);

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.maxCount()).isEqualTo(6);
        assertThat(firstChoice.validIds()).containsExactlyElementsOf(ids(player1Lands));
        harness.handleMultiplePermanentsChosen(player1, ids(player1Lands.subList(0, 6)));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactlyElementsOf(ids(player2Lands));
        harness.handleMultiplePermanentsChosen(player2, ids(player2Lands.subList(0, 6)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player1Lands.subList(0, 6)));
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player2Lands.subList(0, 6)));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Creature.getCard());
    }

    @Test
    @DisplayName("Declining the search still destroys nonland permanents and keeps all lands")
    void decliningSearchStillDestroysOtherPermanents() {
        UrzasSylex sylex = new UrzasSylex();
        harness.addToBattlefield(player1, sylex);
        List<Permanent> player1Lands = addPlains(player1, 2);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Permanent> player2Lands = addPlains(player2, 2);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player1Lands));
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player2Lands));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Creature.getCard());
    }

    private List<Permanent> addPlains(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lands.add(harness.addToBattlefieldAndReturn(player, new Plains()));
        }
        return lands;
    }

    private List<UUID> ids(List<Permanent> permanents) {
        return permanents.stream().map(Permanent::getId).toList();
    }
}

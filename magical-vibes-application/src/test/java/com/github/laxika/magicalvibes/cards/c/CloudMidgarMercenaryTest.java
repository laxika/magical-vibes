package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MageSlayer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudMidgarMercenary.class, LeoninScimitar.class, MageSlayer.class, GrizzlyBears.class})
class CloudMidgarMercenaryTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud searches for an Equipment and puts the chosen card into hand")
    void searchesForEquipment() {
        harness.setHand(player1, List.of(new CloudMidgarMercenary()));
        harness.setLibrary(player1, List.of(new LeoninScimitar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Leonin Scimitar");
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cloud doubles a trigger from an Equipment attached to it")
    void doublesAttachedEquipmentTrigger() {
        harness.setLife(player2, 20);
        Permanent cloud = addReadyPermanent(player1, new CloudMidgarMercenary());
        Permanent slayer = addPermanent(player2, new MageSlayer());
        slayer.setAttachedTo(cloud.getId());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() == slayer.getCard())
                .hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cloud does not double a trigger from an Equipment attached elsewhere")
    void doesNotDoubleEquipmentAttachedElsewhere() {
        harness.setLife(player2, 20);
        Permanent cloud = addReadyPermanent(player1, new CloudMidgarMercenary());
        Permanent scimitar = addPermanent(player1, new LeoninScimitar());
        scimitar.setAttachedTo(cloud.getId());
        Permanent bears = addReadyPermanent(player1, new GrizzlyBears());
        Permanent slayer = addPermanent(player1, new MageSlayer());
        slayer.setAttachedTo(bears.getId());

        declareAttackers(player1, List.of(0, 2));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() == slayer.getCard())
                .hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = addPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

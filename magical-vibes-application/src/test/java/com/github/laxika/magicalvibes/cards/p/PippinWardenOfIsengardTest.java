package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeastingTrollKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PippinWardenOfIsengard.class, FeastingTrollKing.class, GrizzlyBears.class})
class PippinWardenOfIsengardTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Merry")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card merry = namedCard("Merry, Warden of Isengard");
        harness.setLibrary(player2, List.of(merry));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new PippinWardenOfIsengard());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(merry);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first ability creates a Food token")
    void createsFoodToken() {
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, pippin), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(pippin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing four Foods boosts other creatures and gives them haste")
    void sacrificesFourFoodsForTeamBoostAndHaste() {
        createThreeFoods();
        Permanent pippin = addCreatureReady(player1, new PippinWardenOfIsengard());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, pippin), 0, null, null);
        harness.passBothPriorities();
        harness.performUntapStep(player1);

        assertThat(findPermanents(player1, "Food")).hasSize(4);

        harness.activateAbility(player1, indexOf(player1, pippin), 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pippin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pippin, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private void createThreeFoods() {
        harness.setHand(player1, List.of(new FeastingTrollKing()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}

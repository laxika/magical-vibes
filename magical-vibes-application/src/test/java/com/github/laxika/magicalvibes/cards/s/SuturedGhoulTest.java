package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuturedGhoulTest extends BaseCardTest {

    private void castGhoul() {
        harness.setHand(player1, new ArrayList<>(List.of(new SuturedGhoul())));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent ghoul() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sutured Ghoul"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Power and toughness equal the total power and toughness of the exiled creature cards")
    void powerToughnessSumExiledCards() {
        GrizzlyBears bears = new GrizzlyBears();   // 2/2
        GiantSpider spider = new GiantSpider();    // 2/4
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, spider)));

        castGhoul();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), spider.getId()));

        Permanent ghoul = ghoul();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getId())
                .contains(bears.getId(), spider.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen cards are exiled; unchosen creature cards stay in the graveyard")
    void unchosenCardsStayInGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();   // 2/2
        HillGiant giant = new HillGiant();         // 3/3
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, giant)));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        Permanent ghoul = ghoul();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getId())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Exiling nothing leaves a 0/0 that dies to state-based actions")
    void exilingNothingDiesAsZeroZero() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sutured Ghoul"));
    }

    @Test
    @DisplayName("Noncreature cards in the graveyard can't be exiled")
    void noncreatureCardsAreNotOffered() {
        Pacifism pacifism = new Pacifism();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(pacifism, bears)));

        castGhoul();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("With an empty graveyard the ghoul enters as a 0/0 with no choice offered")
    void emptyGraveyardNoChoice() {
        castGhoul();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
    }
}

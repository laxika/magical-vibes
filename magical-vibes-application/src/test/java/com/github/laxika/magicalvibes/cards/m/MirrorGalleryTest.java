package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KiraGreatGlassSpinner;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorGallery.class, KiraGreatGlassSpinner.class, SongOfTheDryads.class})
class MirrorGalleryTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate legendary permanents survive while Mirror Gallery is on the battlefield")
    void duplicateLegendsSurvive() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Mirror Gallery protects duplicate legends controlled by another player")
    void protectsLegendsControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new MirrorGallery());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("The legend rule returns when Mirror Gallery leaves the battlefield")
    void legendRuleReturnsWhenGalleryLeaves() {
        Permanent gallery = harness.addToBattlefieldAndReturn(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        gd.playerBattlefields.get(player1.getId()).remove(gallery);

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }

    @Test
    @DisplayName("A tapped Mirror Gallery still protects duplicate legends")
    void tappedGalleryStillProtectsLegends() {
        Permanent gallery = harness.addToBattlefieldAndReturn(player1, new MirrorGallery());
        gallery.setTapped(true);
        harness.addToBattlefield(player2, new KiraGreatGlassSpinner());
        harness.addToBattlefield(player2, new KiraGreatGlassSpinner());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Removing one of two Galleries leaves the legend rule disabled")
    void anotherGalleryContinuesToProtectLegends() {
        Permanent firstGallery = harness.addToBattlefieldAndReturn(player1, new MirrorGallery());
        harness.addToBattlefield(player2, new MirrorGallery());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        harness.addToBattlefield(player1, new KiraGreatGlassSpinner());
        gd.playerBattlefields.get(player1.getId()).remove(firstGallery);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Song of the Dryads removes Mirror Gallery's legend rule exemption")
    void galleryLosingItsAbilityRestoresLegendRule() {
        Permanent gallery = harness.addToBattlefieldAndReturn(player1, new MirrorGallery());
        Permanent keptLegend = harness.addToBattlefieldAndReturn(player1, new KiraGreatGlassSpinner());
        Permanent otherLegend = harness.addToBattlefieldAndReturn(player1, new KiraGreatGlassSpinner());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, gallery.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
        harness.handlePermanentChosen(player1, keptLegend.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keptLegend).doesNotContain(otherLegend);
        harness.assertInGraveyard(player1, "Kira, Great Glass-Spinner");
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CutthroatCenturion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LocusOfEnlightenment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEnigmaJewel.class, LocusOfEnlightenment.class, CutthroatCenturion.class,
        Forest.class, GrizzlyBears.class})
class TheEnigmaJewelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        TheEnigmaJewel jewel = new TheEnigmaJewel();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromHand(player1, jewel, "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(Permanent::isTapped).isEqualTo(true);
    }

    @Test
    @DisplayName("Craft accepts four nonlands with activated abilities from the battlefield and graveyard")
    void craftsWithMixedActivatedAbilityMaterials() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new TheEnigmaJewel());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        Card graveyardCenturion = new CutthroatCenturion();
        harness.setGraveyard(player1, List.of(graveyardCenturion, new Forest(), new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jewel);
        assertThat(locus.getCard()).isInstanceOf(LocusOfEnlightenment.class);
        assertThat(gd.getCardsExiledByPermanent(locus.getId()))
                .hasSize(4)
                .allMatch(card -> card instanceof CutthroatCenturion);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Gained activated abilities are copied and each gained ability is usable only once each turn")
    void copiesGainedAbilityAndLimitsEachAbility() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new TheEnigmaJewel());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jewel);
        assertThat(locus.getCard()).isInstanceOf(LocusOfEnlightenment.class);
        harness.clearPriorityPassed();
        int locusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locus);
        harness.activateAbility(player1, locusIndex, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        resolveAllTriggers();

        assertThat(locus.getPowerModifier()).isEqualTo(4);
        assertThat(locus.getToughnessModifier()).isEqualTo(4);

        harness.clearPriorityPassed();
        int updatedLocusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locus);
        assertThatThrownBy(() -> harness.activateAbility(player1, updatedLocusIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}

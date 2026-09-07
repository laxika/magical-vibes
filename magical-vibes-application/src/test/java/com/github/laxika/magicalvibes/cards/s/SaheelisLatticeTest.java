package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BelligerentYearling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MastercraftRaptor;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StampedingHorncrest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaheelisLattice.class, MastercraftRaptor.class, GrizzlyBears.class, Mountain.class,
        BelligerentYearling.class, StampedingHorncrest.class})
class SaheelisLatticeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering offers a discard and then draws two cards")
    void acceptsDiscardAndDrawsTwo() {
        Card firstDraw = new Mountain();
        Card secondDraw = new Mountain();
        setDeck(List.of(firstDraw, secondDraw));

        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new SaheelisLattice(), discarded)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Craft exiles one or more Dinosaurs and returns Mastercraft Raptor transformed")
    void craftsWithMultipleDinosaursAndUsesTheirTotalPower() {
        Permanent lattice = harness.addToBattlefieldAndReturn(player1, new SaheelisLattice());
        Permanent battlefieldDinosaur = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        BelligerentYearling graveyardDinosaur = new BelligerentYearling();
        harness.setGraveyard(player1, List.of(graveyardDinosaur));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CraftMaterialChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(battlefieldDinosaur.getCard().getId(), graveyardDinosaur.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lattice, battlefieldDinosaur);
        assertThat(gd.findExiledCard(battlefieldDinosaur.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(graveyardDinosaur.getId())).isNotNull();

        Permanent raptor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof MastercraftRaptor)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(4);
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BelligerentYearling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MastercraftRaptor;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StampedingHorncrest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheelisLattice.class, MastercraftRaptor.class, GrizzlyBears.class, Mountain.class,
        BelligerentYearling.class, StampedingHorncrest.class})
class SaheelisLatticeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering offers a discard and then draws two cards")
    void acceptsDiscardAndDrawsTwo() {
        Card firstDraw = new Mountain();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

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

    @Test
    void decliningDiscardDoesNotDraw() {
        Mountain kept = new Mountain();
        Mountain libraryCard = new Mountain();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SaheelisLattice(), kept));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void emptyHandCannotDrawWithoutDiscarding() {
        Mountain libraryCard = new Mountain();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SaheelisLattice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void craftsWithOneGraveyardDinosaur() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        BelligerentYearling material = new BelligerentYearling();
        harness.setGraveyard(player1, List.of(material));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Saheeli's Lattice");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mastercraft Raptor");
        Permanent raptor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(3);
    }

    @Test
    void materialCountersDoNotContributeToPowerInExile() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new BelligerentYearling());
        material.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mastercraft Raptor");
        Permanent raptor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(3);
    }

    @Test
    void cannotCraftWithoutDinosaurMaterials() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        harness.addToBattlefield(player2, new BelligerentYearling());
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new BelligerentYearling()));
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Saheeli's Lattice");
    }

    @Test
    void canChooseOnlyOneOfSeveralAvailableDinosaurs() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        BelligerentYearling selected = new BelligerentYearling();
        BelligerentYearling unselected = new BelligerentYearling();
        harness.setGraveyard(player1, List.of(selected, unselected));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.findExiledCard(selected.getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Mastercraft Raptor");
        assertThat(gqs.getEffectivePower(gd, gd.playerBattlefields.get(player1.getId()).getFirst()))
                .isEqualTo(3);
    }

    @Test
    void cannotCraftDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new SaheelisLattice());
        harness.setGraveyard(player1, List.of(new BelligerentYearling()));
        addCraftMana();
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Saheeli's Lattice");
    }
}

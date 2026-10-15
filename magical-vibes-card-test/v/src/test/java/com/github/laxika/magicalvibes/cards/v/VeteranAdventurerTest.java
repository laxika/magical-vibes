package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KabiraOutrider;
import com.github.laxika.magicalvibes.cards.m.MalakirBloodPriest;
import com.github.laxika.magicalvibes.cards.m.MerfolkWindrobber;
import com.github.laxika.magicalvibes.cards.t.TajuruParagon;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteranAdventurer.class, KabiraOutrider.class, MerfolkWindrobber.class,
        TazeemRoilmage.class, MalakirBloodPriest.class, TajuruParagon.class})
class VeteranAdventurerTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces the generic cost by four")
    void fullPartyReducesCostByFour() {
        addFullParty();
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without a party, the full generic cost is required")
    void withoutPartyRequiresFullGenericCost() {
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Is also a Cleric, Rogue, Warrior, and Wizard")
    void hasAllPartySubtypes() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranAdventurer());

        assertThat(gqs.effectiveCreatureSubtypes(gd, veteran))
                .contains(CardSubtype.HUMAN, CardSubtype.CLERIC, CardSubtype.ROGUE,
                        CardSubtype.WARRIOR, CardSubtype.WIZARD);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    void eachVeteranOnBattlefieldFillsOnlyOnePartyRole(int creatureCount) {
        for (int i = 0; i < creatureCount; i++) {
            harness.addToBattlefield(player1, new VeteranAdventurer());
        }
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5 - Math.min(creatureCount, 4));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void partyCannotReduceCostBeyondNumberOfDistinctCreatures(int creatureCount) {
        for (int i = 0; i < creatureCount; i++) {
            harness.addToBattlefield(player1, new VeteranAdventurer());
        }
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4 - creatureCount);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleClericsFillOnlyOnePartyRole() {
        harness.addToBattlefield(player1, new MalakirBloodPriest());
        harness.addToBattlefield(player1, new MalakirBloodPriest());
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsCreaturesDoNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new VeteranAdventurer());
        }
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fullPartyDoesNotRemoveGreenManaRequirement() {
        addFullParty();
        harness.setHand(player1, List.of(new VeteranAdventurer()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addedCreatureTypesApplyInLibrary() {
        VeteranAdventurer veteran = new VeteranAdventurer();
        harness.setLibrary(player1, List.of(veteran));
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(veteran.getId());

        harness.handleMultipleCardsChosen(player1, List.of(veteran.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(veteran);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingDoesNotTapVeteran() {
        Permanent veteran = addCreatureReady(player1, new VeteranAdventurer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(veteran.isAttacking()).isTrue();
        assertThat(veteran.isTapped()).isFalse();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new MalakirBloodPriest());
        harness.addToBattlefield(player1, new MerfolkWindrobber());
        harness.addToBattlefield(player1, new KabiraOutrider());
        harness.addToBattlefield(player1, new TazeemRoilmage());
    }
}

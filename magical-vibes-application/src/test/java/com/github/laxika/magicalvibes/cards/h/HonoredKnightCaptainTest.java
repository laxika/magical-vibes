package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonoredKnightCaptain.class, Hullcarver.class, Hylderblade.class})
class HonoredKnightCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 white Human Soldier token")
    void etbCreatesHumanSoldierToken() {
        harness.setHand(player1, List.of(new HonoredKnightCaptain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing this creature puts a searched Equipment onto the battlefield")
    void sacrificeSearchesForEquipment() {
        Permanent source = addReadySource();
        harness.setLibrary(player1, List.of(new Hylderblade(), new Hullcarver()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Honored Knight-Captain");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Hylderblade");

        harness.handleCardChosen(player1, 0);

        Permanent equipment = findPermanent(player1, "Hylderblade");
        assertThat(equipment).isNotNull();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hullcarver");
    }

    @Test
    @DisplayName("The sacrifice ability resolves without an Equipment in the library")
    void sacrificeSearchFindsNoEquipment() {
        addReadySource();
        harness.setLibrary(player1, List.of(new Hullcarver()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Honored Knight-Captain");
        harness.assertNotOnBattlefield(player1, "Hullcarver");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Knight-Captain can activate its sacrifice ability")
    void tappedSummoningSickSourceCanActivate() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HonoredKnightCaptain());
        source.tap();
        harness.setLibrary(player1, List.of(new Hylderblade()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Honored Knight-Captain");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hylderblade");
        assertThat(findPermanent(player1, "Hylderblade").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller may find no Equipment even when one is in the library")
    void canFailToFindEquipment() {
        addReadySource();
        Hylderblade equipment = new Hylderblade();
        harness.setLibrary(player1, List.of(equipment, new Hullcarver()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Honored Knight-Captain");
        harness.assertNotOnBattlefield(player1, "Hylderblade");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(equipment);
    }

    @Test
    @DisplayName("Colorless mana cannot replace the second white mana in the activation cost")
    void abilityRequiresTwoWhiteMana() {
        Permanent source = addReadySource();
        harness.setLibrary(player1, List.of(new Hylderblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertNotInGraveyard(player1, "Honored Knight-Captain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger still creates its token after Knight-Captain is sacrificed")
    void enterTriggerResolvesAfterSourceIsSacrificed() {
        harness.setHand(player1, List.of(new HonoredKnightCaptain()));
        harness.setLibrary(player1, List.of(new Hylderblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Honored Knight-Captain");
        harness.assertOnBattlefield(player1, "Hylderblade");
        harness.assertOnBattlefield(player1, "Human Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private Permanent addReadySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HonoredKnightCaptain());
        source.setSummoningSick(false);
        return source;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

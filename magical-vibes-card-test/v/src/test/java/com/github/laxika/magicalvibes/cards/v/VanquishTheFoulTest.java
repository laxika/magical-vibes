package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossusOfAkros;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanquishTheFoul.class, AirElemental.class, HillGiant.class, ColossusOfAkros.class})
class VanquishTheFoulTest extends BaseCardTest {

    private void addManaAndCast(UUID targetId) {
        harness.setHand(player1, List.of(new VanquishTheFoul()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys a creature with power 4 or greater and offers scry 1")
    void destroysHighPowerCreatureAndOffersScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Completing scry 1 finishes resolving Vanquish the Foul")
    void completingScryFinishesSpell() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> addManaAndCast(giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void canDestroyOwnCreatureAndKeepScryCardOnTop() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card first = new VanquishTheFoul();
        Card second = new VanquishTheFoul();
        harness.setLibrary(player1, List.of(first, second));

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void indestructibleTargetSurvivesButScryCanBottomCard() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player2, new ColossusOfAkros());
        Card first = new VanquishTheFoul();
        Card second = new VanquishTheFoul();
        harness.setLibrary(player1, List.of(first, second));

        addManaAndCast(colossus.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Colossus of Akros");
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void powerDroppingBelowFourMakesSpellFailToResolveWithoutScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card top = new VanquishTheFoul();
        harness.setLibrary(player1, List.of(top));

        addManaAndCast(elemental.getId());
        elemental.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void missingTargetMakesSpellFailToResolveWithoutScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card top = new VanquishTheFoul();
        harness.setLibrary(player1, List.of(top));

        addManaAndCast(elemental.getId());
        gd.playerBattlefields.get(player2.getId()).remove(elemental);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void usesModifiedPowerToDetermineLegalTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setPowerModifier(1);
        harness.setLibrary(player1, List.of());

        addManaAndCast(giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void destructionStillResolvesWithEmptyLibrary() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setLibrary(player1, List.of());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }

    @Test
    void regenerationSavesTargetAndDoesNotPreventScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.setRegenerationShield(1);
        harness.setLibrary(player1, List.of(new VanquishTheFoul()));

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(elemental.isTapped()).isTrue();
        assertThat(elemental.getRegenerationShield()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertInGraveyard(player1, "Vanquish the Foul");
    }
}

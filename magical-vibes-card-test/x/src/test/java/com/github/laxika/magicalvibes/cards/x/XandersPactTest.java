package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XandersPact.class, Shock.class, Forest.class, GrizzlyBears.class,
        Fireball.class, SphereOfResistance.class})
class XandersPactTest extends BaseCardTest {

    @Test
    void exilesEachOpponentsTopCardAndGrantsLifeCastPermissionToNonlands() {
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(shock, forest));
        castPact();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        assertThat(gd.exilePlayForLifeEqualToManaValue).contains(shock.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(forest.getId());
    }

    @Test
    void castsAnExiledSpellByPayingLifeEqualToManaValue() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        castPact();

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(bears));
    }

    @Test
    void casualtyCopiesTheExileEffect() {
        Shock first = new Shock();
        Shock second = new Shock();
        Permanent casualtyCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new XandersPact()));
        addPactMana();

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }


    @Test
    void exiledPactCanBeCastWithoutPayingOptionalCasualty() {
        XandersPact exiledPact = new XandersPact();
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(exiledPact, shock));
        castPact();

        harness.castFromExile(player1, exiledPact.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
        harness.assertInGraveyard(player2, "Xander's Pact");
    }

    @Test
    void lifePaymentDoesNotWaiveSpellCostIncreases() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        castPact();
        harness.addToBattlefield(player2, new SphereOfResistance());

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        harness.assertLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void lifeAlternativeCannotCastFireballWithPositiveX() {
        Fireball fireball = new Fireball();
        harness.setLibrary(player2, List.of(fireball));
        castPact();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, fireball.getId(), 5, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(fireball);
        harness.assertLife(player1, 20);
    }

    @Test
    void exilesLandWithoutAllowingItToBePlayed() {
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        castPact();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(forest);
        assertThatThrownBy(() -> harness.castFromExile(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void insufficientLifeLeavesSpellInExile() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        castPact();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        harness.assertLife(player1, 1);
    }


    @Test
    void emptyOpponentLibraryDoesNotExileControllersCards() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setLibrary(player2, List.of());
        castPact();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void creatureStillRequiresSorceryTiming() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        castPact();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        harness.assertLife(player1, 20);
    }

    @Test
    void permissionExpiresAfterTheTurn() {
        Shock shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        castPact();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
    }

    private void castPact() {
        harness.setHand(player1, List.of(new XandersPact()));
        addPactMana();
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void addPactMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

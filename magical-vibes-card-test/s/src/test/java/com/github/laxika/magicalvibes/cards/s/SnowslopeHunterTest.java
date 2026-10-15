package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowslopeHunter.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class SnowslopeHunterTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndExilesTopCardWithNextTurnPermission() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addReadyHunter();
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareYourTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer)
                .containsEntry(topCard.getId(), player1.getId());

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void sacrificesAnotherArtifact() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        addReadyHunter();
        harness.addToBattlefield(player1, new Spellbook());
        prepareYourTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void canActivateOnlyOnceEachTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addReadyHunter();
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        prepareYourTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    @Test
    void cannotActivateDuringOpponentTurn() {
        addReadyHunter();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    void cannotActivateWithoutAnotherCreatureOrArtifact() {
        addReadyHunter();
        prepareYourTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact");
    }

    @Test
    void canActivateWhileSummoningSickDuringYourEndStep() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SnowslopeHunter());
        harness.addToBattlefield(player1, new SnowslopeHunter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snowslope Hunter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyLibraryStillRequiresSacrifice() {
        harness.setLibrary(player1, List.of());
        addReadyHunter();
        harness.addToBattlefield(player1, new SnowslopeHunter());
        prepareYourTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snowslope Hunter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotSacrificeAnOpponentsCreatureOrYourLand() {
        addReadyHunter();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SnowslopeHunter());
        prepareYourTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Snowslope Hunter");
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        Card topCard = new SnowslopeHunter();
        harness.setLibrary(player1, List.of(topCard));
        addReadyHunter();
        harness.addToBattlefield(player1, new SnowslopeHunter());
        prepareYourTurn();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void permissionLastsThroughYourNextTurnAndThenExpires() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        addReadyHunter();
        harness.addToBattlefield(player1, new SnowslopeHunter());
        prepareYourTurn();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private Permanent addReadyHunter() {
        return addCreatureReady(player1, new SnowslopeHunter());
    }

    private void prepareYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}

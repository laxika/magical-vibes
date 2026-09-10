package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
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
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(topCard.getId(), gd.turnNumber + 2);

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

    private Permanent addReadyHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new SnowslopeHunter());
        hunter.setSummoningSick(false);
        return hunter;
    }

    private void prepareYourTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}

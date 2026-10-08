package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitheredWretch.class, FugitiveWizard.class, TormodsCrypt.class})
class WitheredWretchTest extends BaseCardTest {

    @Test
    void exilesTargetCardFromControllersGraveyard() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player1, new ArrayList<>(List.of(wizard)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wizard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wizard);
    }

    @Test
    void exilesTargetCardFromOpponentsGraveyard() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player2, new ArrayList<>(List.of(wizard)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(wizard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(wizard);
    }

    @Test
    void rejectsTargetNotInAnyGraveyard() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCardOnBattlefieldAsGraveyardTarget() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.addToBattlefield(player2, wizard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresAGraveyardTarget() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player1, new ArrayList<>(List.of(wizard)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player2, new ArrayList<>(List.of(wizard)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(wizard);
    }

    @Test
    void exilesOnlyTheTargetedNoncreatureCardAndAcceptsColoredMana() {
        Card crypt = new TormodsCrypt();
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player2, List.of(crypt, wizard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, crypt.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(wizard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(crypt);
    }

    @Test
    void cannotActivateWithoutPayingOneMana() {
        Card wizard = new FugitiveWizard();
        harness.addToBattlefield(player1, new WitheredWretch());
        harness.setGraveyard(player2, List.of(wizard));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(wizard);
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Card first = new FugitiveWizard();
        Card second = new FugitiveWizard();
        var wretch = harness.addToBattlefieldAndReturn(player1, new WitheredWretch());
        wretch.setTapped(true);
        wretch.setSummoningSick(true);
        harness.setGraveyard(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(wretch.isTapped()).isTrue();
    }

    @Test
    void resolvesAfterWitheredWretchDies() {
        Card wizard = new FugitiveWizard();
        Card source = new WitheredWretch();
        var wretch = harness.addToBattlefieldAndReturn(player1, source);
        harness.setGraveyard(player2, List.of(wizard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, wizard.getId(), Zone.GRAVEYARD);
        wretch.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(wizard);
    }
}

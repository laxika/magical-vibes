package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpeditedInheritance.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        RayOfCommand.class})
class ExpeditedInheritanceTest extends BaseCardTest {

    @Test
    void damagedCreatureControllerMayExileThatManyCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player2.getId())
                .containsEntry(second.getId(), player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningDoesNotExileCards() {
        Forest top = new Forest();
        harness.setLibrary(player2, List.of(top));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    @Test
    void damagedCreatureControllerIsCapturedBeforeLethalDamageRemovesCreature() {
        Forest top = new Forest();
        harness.setLibrary(player2, List.of(top));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player2.getId());
    }

    @Test
    void acceptingAfterControlChangesExilesFromCurrentControllersLibrary() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest opponentsTop = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentsTop));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        var creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new RayOfCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void exiledInstantRequiresManaAndCanBeCastByDamagedCreaturesController() {
        Shock top = new Shock();
        harness.setLibrary(player2, List.of(top));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThatThrownBy(() -> harness.castFromExile(player2, top.getId(), player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromExile(player2, top.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exiledLandCanBePlayedDuringControllersMainPhase() {
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void permissionExpiresAfterOpponentsNextTurn() {
        Forest top = new Forest();
        harness.setLibrary(player2, List.of(top, new Forest(), new Forest()));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player2.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
    }

    @Test
    void permissionSurvivesCurrentTurnAndExpiresAfterControllersNextTurn() {
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest()));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void damageToPlayerDoesNotTriggerInheritance() {
        Forest top = new Forest();
        harness.setLibrary(player2, List.of(top));
        harness.addToBattlefield(player1, new ExpeditedInheritance());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }
}

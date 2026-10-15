package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitizensArrest.class, ChandraNalaar.class, FlickerOfFate.class, Forest.class, FountainOfYouth.class,
        GrizzlyBears.class, Naturalize.class})
class CitizensArrestTest extends BaseCardTest {

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CitizensArrest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB exiles target planeswalker an opponent controls")
    void etbExilesOpponentPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        castAndResolve(chandra.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chandra);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Chandra Nalaar"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Citizen's Arrest leaves the battlefield")
    void exiledPermanentReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID arrestId = harness.getPermanentId(player1, "Citizen's Arrest");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, arrestId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's noncreature, nonplaneswalker permanent")
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature the caster controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target stays on the battlefield if Citizen's Arrest leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpCast();
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID arrestId = harness.getPermanentId(player1, "Citizen's Arrest");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, arrestId);
        harness.assertInGraveyard(player1, "Citizen's Arrest");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Citizen's Arrest can resolve without any legal target for its trigger")
    void resolvesWithoutLegalTargets() {
        setUpCast();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Citizen's Arrest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a planeswalker the caster controls")
    void cannotTargetOwnPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, chandra.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning Citizen's Arrest is a new source and cannot exile the original trigger's target")
    void blinkedSourceDoesNotExileOriginalTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        setUpCast();
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        UUID originalArrestId = harness.getPermanentId(player1, "Citizen's Arrest");
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, originalArrestId);
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Citizen's Arrest")).isNotEqualTo(originalArrestId);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(chandra);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Chandra Nalaar"))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}

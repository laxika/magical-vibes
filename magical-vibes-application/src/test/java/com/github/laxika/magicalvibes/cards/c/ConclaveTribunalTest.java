package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DisplacerKitten;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealersHawk;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclaveTribunal.class, Forest.class, GrizzlyBears.class, Naturalize.class,
        HealersHawk.class, DisplacerKitten.class})
class ConclaveTribunalTest extends BaseCardTest {

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ConclaveTribunal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls until source leaves")
    void etbExilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Conclave Tribunal leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Conclave Tribunal");

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sourceId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent's nonland permanent can be targeted")
    void rejectsLandAndOwnPermanentTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID landId = harness.getPermanentId(player2, "Forest");
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");

        prepareToCast();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ConclaveTribunal()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Tribunal before its ETB resolves leaves the target on the battlefield")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Conclave Tribunal");
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Conclave Tribunal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment target removed in response is not exiled")
    void targetLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new ConclaveTribunal());
        UUID targetId = harness.getPermanentId(player2, "Conclave Tribunal");
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Conclave Tribunal");
        harness.assertInGraveyard(player2, "Conclave Tribunal");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The original ETB does nothing after Tribunal leaves and returns")
    void originalTriggerDoesNotUseReturnedTribunalAsSource() {
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var newTarget = harness.addToBattlefieldAndReturn(player2, new HealersHawk());
        prepareToCast();
        harness.castEnchantment(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        UUID originalSourceId = harness.getPermanentId(player1, "Conclave Tribunal");

        harness.addToBattlefield(player1, new DisplacerKitten());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, originalSourceId);
        harness.handlePermanentChosen(player1, originalSourceId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, newTarget.getId());

        assertThat(harness.getPermanentId(player1, "Conclave Tribunal"))
                .isNotEqualTo(originalSourceId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(originalTarget.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Healer's Hawk");
        harness.assertOnBattlefield(player1, "Conclave Tribunal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four newly entered white creatures can convoke Tribunal without mana")
    void convokePaysColoredAndGenericManaWithSummoningSickCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ConclaveTribunal()));
        var first = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        var second = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        var third = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        var fourth = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        for (var creature : List.of(first, second, third, fourth)) {
            creature.setSummoningSick(true);
        }
        harness.addToBattlefield(player2, new HealersHawk());
        UUID targetId = harness.getPermanentId(player2, "Healer's Hawk");

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(List.of(first, second, third, fourth)).allMatch(creature -> creature.isTapped());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Conclave Tribunal");
        harness.assertNotOnBattlefield(player2, "Healer's Hawk");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof HealersHawk);
    }
}

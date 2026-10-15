package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicPurge;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.BiolumeSerpent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BiolumeEgg.class, BiolumeSerpent.class, AngelicPurge.class, Island.class})
class BiolumeEggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB scries 2")
    void etbScries2() {
        harness.setHand(player1, List.of(new BiolumeEgg()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB scry

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing returns transformed at next end step")
    void sacrificeReturnsTransformedAtNextEndStep() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BiolumeEgg());

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), egg.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Biolume Egg");
        harness.assertInGraveyard(player1, "Biolume Egg");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent serpent = findPermanent(player1, "Biolume Serpent");
        assertThat(serpent.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Destroying does not return transformed")
    void destroyDoesNotReturn() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, egg, false));

        harness.assertInGraveyard(player1, "Biolume Egg");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Biolume Serpent");
    }

    @Test
    @DisplayName("Serpent becomes unblockable by sacrificing two Islands")
    void serpentUnblockableBySacrificingIslands() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, indexOf(serpent), null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Island"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Serpent cannot activate without two Islands")
    void serpentCannotActivateWithoutTwoIslands() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(serpent), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Delayed return uses the stack before the Egg returns")
    void delayedReturnUsesStack() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> sacrificeEgg(egg));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passUntil(TurnStep.END_STEP));

        harness.assertInGraveyard(player1, "Biolume Egg");
        harness.assertNotOnBattlefield(player1, "Biolume Serpent");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Biolume Serpent");
        harness.assertNotInGraveyard(player1, "Biolume Egg");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificed opponent-owned Egg returns to its owner")
    void sacrificedStolenEggReturnsToOwner() {
        BiolumeEgg card = new BiolumeEgg();
        card.setOwnerId(player2.getId());
        Permanent egg = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(egg.getId(), player2.getId());
        sacrificeEgg(egg);
        harness.assertInGraveyard(player2, "Biolume Egg");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Biolume Serpent");
        assertThat(findPermanent(player2, "Biolume Serpent").isTransformed()).isTrue();
        harness.assertNotInGraveyard(player2, "Biolume Egg");
    }

    @Test
    @DisplayName("Egg removed from the graveyard before the end step does not return")
    void removedEggDoesNotReturn() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        sacrificeEgg(egg);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(egg.getOriginalCard()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Biolume Serpent");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(egg.getOriginalCard());
    }

    @Test
    @DisplayName("Opponent's Islands cannot pay Serpent's activation cost")
    void opponentsIslandsCannotPayCost() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(serpent), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
        assertThat(countPermanents(player2, "Island")).isEqualTo(2);
    }

    @Test
    @DisplayName("Serpent's unblockability expires at cleanup")
    void unblockabilityExpiresAtCleanup() {
        Permanent serpent = addTransformedSerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.activateAbility(player1, indexOf(serpent), null, null);
        resolveAllTriggers();
        assertThat(serpent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Scry two with a one-card library offers only that card")
    void scryWithOneCardLibrary() {
        Island top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new BiolumeEgg()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
    }
    private void sacrificeEgg(Permanent egg) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BiolumeEgg());
        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), egg.getId());
        resolveAllTriggers();
    }

    private Permanent addTransformedSerpent() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new BiolumeEgg());
        BiolumeSerpent back = new BiolumeSerpent();
        back.setSetCode(perm.getOriginalCard().getSetCode());
        back.setCollectorNumber(perm.getOriginalCard().getCollectorNumber());
        perm.setCard(back);
        perm.setTransformed(true);
        return perm;
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

}

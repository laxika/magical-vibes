package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrudgeSpellTest extends BaseCardTest {

    private void createSkeleton() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {B} and exiling two creature cards creates a Skeleton token")
    void createsSkeletonToken() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        createSkeleton();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without two creature cards in the graveyard")
    void requiresTwoCreatureCards() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Skeleton token can regenerate itself for {B}")
    void skeletonRegenerates() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        createSkeleton();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, findPermanents(player1, "Skeleton").getFirst().getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
    }

    @Test
    @DisplayName("All Skeleton tokens are destroyed when Drudge Spell leaves the battlefield")
    void destroysSkeletonsOnLeave() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        createSkeleton();
        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, findPermanents(player1, "Drudge Spell").getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Spell");
        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield does not save the Skeletons from the leave trigger")
    void skeletonsCannotBeRegeneratedOnLeave() {
        harness.addToBattlefield(player1, new DrudgeSpell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        createSkeleton();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, findPermanents(player1, "Drudge Spell").getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Skeleton")).isZero();
    }
}

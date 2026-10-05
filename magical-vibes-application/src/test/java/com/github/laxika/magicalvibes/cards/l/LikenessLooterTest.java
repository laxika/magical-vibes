package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BakeryRaid;
import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.h.HollowScavenger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LikenessLooter.class, GrizzlyBears.class, HillGiant.class, Island.class, Clone.class,
        HollowScavenger.class, BakeryRaid.class, CandyTrail.class})
class LikenessLooterTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws a card, then prompts for a discard")
    void tapAbilityDrawsThenDiscards() {
        addCreatureReady(player1, new LikenessLooter());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copy ability copies an own-graveyard creature, adds flying, and retains the ability")
    void copiesOwnGraveyardCreatureWithFlyingAndAbility() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(looter.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, looter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, looter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, looter, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, 4, giant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(looter.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Copy ability cannot target an opponent's graveyard")
    void copyAbilityTargetsOnlyOwnGraveyard() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(looter.getCard().getName()).isEqualTo("Likeness Looter");
    }

    @Test
    void copyAbilityRejectsWrongManaValue() {
        addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 3, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyAbilityRejectsNoncreatureEvenWithMatchingManaValue() {
        addCreatureReady(player1, new LikenessLooter());
        Island island = new Island();
        harness.setGraveyard(player1, List.of(island));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, island.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyAbilityCannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyAbilityCannotActivateWhileAnotherAbilityIsOnStack() {
        addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Island");
    }

    @Test
    void copyAbilityDoesNothingIfTargetLeavesGraveyard() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));

        harness.passBothPriorities();

        assertThat(looter.getCard().getName()).isEqualTo("Likeness Looter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingCreatureDoesNotTriggerItsEntersAbility() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        Clone clone = new Clone();
        harness.setGraveyard(player1, List.of(clone));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, 4, clone.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(looter);
        harness.assertInGraveyard(player1, "Likeness Looter");
    }

    @Test
    void cloneOfLikenessLooterCanUseAndRetainCopyAbility() {
        Permanent original = addCreatureReady(player2, new LikenessLooter());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent clone = findPermanent(player1, "Likeness Looter");
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, giant));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(clone.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, clone, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 0, 4, giant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(clone.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.hasKeyword(gd, clone, Keyword.FLYING)).isTrue();
    }

    @Test
    void copyingSameCreatureAgainAllowsNewOncePerTurnAbilityToActivate() {
        Permanent looter = addCreatureReady(player1, new LikenessLooter());
        HollowScavenger scavenger = new HollowScavenger();
        harness.setGraveyard(player1, List.of(scavenger));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addToBattlefield(player1, new CandyTrail());

        harness.activateAbility(player1, 0, 1, 3, scavenger.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, looter)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Candy Trail");
        harness.addToBattlefield(player1, new CandyTrail());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.activateAbility(player1, 0, 1, 3, scavenger.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Candy Trail")).isZero();
        assertThat(gqs.getEffectivePower(gd, looter)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, looter)).isEqualTo(6);
    }
}

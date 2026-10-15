package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.cards.s.StreetSpasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivixGuildmage.class, CounselOfTheSoratami.class, GrizzlyBears.class, Island.class,
        Inspiration.class, StreetSpasm.class})
class NivixGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Loot ability draws a card then discards a card")
    void lootAbilityDrawsThenDiscards() {
        addCreatureReady(player1, new NivixGuildmage());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Copy ability copies an instant or sorcery spell you control")
    void copyAbilityCopiesOwnSpell() {
        addCreatureReady(player1, new NivixGuildmage());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot copy a spell controlled by another player")
    void cannotCopyOpponentSpell() {
        addCreatureReady(player1, new NivixGuildmage());

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, counsel, "{2}{U}");

        UUID counselId = counsel.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, counselId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy a creature spell")
    void cannotCopyCreatureSpell() {
        addCreatureReady(player1, new NivixGuildmage());

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        Island drawn = new Island();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    void lootWithEmptyHandDiscardsTheDrawnCard() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        Island drawn = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void canActivateLootTwiceWhileSummoningSick() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new NivixGuildmage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(guildmage.isTapped()).isFalse();
    }

    @Test
    void copyCanChooseANewPlayerTargetWithoutChangingTheOriginal() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        Inspiration inspiration = new Inspiration();
        harness.setHand(player1, List.of(inspiration));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, 1, null, inspiration.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(inspiration);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanKeepTheOriginalTarget() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        Inspiration inspiration = new Inspiration();
        harness.setHand(player1, List.of(inspiration));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, 0, 1, null, inspiration.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(inspiration);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void overloadedCopyPreservesXAndDamagesEveryEligibleCreature() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NivixGuildmage());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NivixGuildmage());
        StreetSpasm streetSpasm = new StreetSpasm();
        harness.setHand(player1, List.of(streetSpasm));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0, 1);
        harness.activateAbility(player1, 0, 1, null, streetSpasm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(streetSpasm);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCopyAnActivatedAbility() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        UUID abilityId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, abilityId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery spell you control");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canCopyASpellCopyAndResolveAllThreeSpells() {
        harness.addToBattlefield(player1, new NivixGuildmage());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island()));
        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();
        UUID firstCopyId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, 1, null, firstCopyId);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(counsel);
        assertThat(gd.stack).isEmpty();
    }
}

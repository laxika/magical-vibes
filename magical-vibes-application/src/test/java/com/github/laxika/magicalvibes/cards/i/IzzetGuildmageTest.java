package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CerebralVortex;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.cards.s.StitchInTime;
import com.github.laxika.magicalvibes.cards.t.TrainOfThought;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzzetGuildmage.class, CerebralVortex.class, Pyromatics.class, StitchInTime.class,
        TrainOfThought.class})
class IzzetGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a low-mana-value instant with the blue ability")
    void copiesInstantWithBlueAbility() {
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        addGuildmage();

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, pyromatics.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> {
                    assertThat(copy.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
                    assertThat(copy.getTargetId()).isEqualTo(player2.getId());
                });
    }

    @Test
    @DisplayName("Copies a low-mana-value sorcery with the red ability")
    void copiesSorceryWithRedAbility() {
        TrainOfThought trainOfThought = new TrainOfThought();
        harness.setHand(player1, List.of(trainOfThought));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        addGuildmage();

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, trainOfThought.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> {
                    assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
                    assertThat(copy.getControllerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    @DisplayName("Offers new targets for a copied instant")
    void offersNewTargetsForCopiedInstant() {
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        addGuildmage();

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, pyromatics.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getTargetId()).isEqualTo(player1.getId()));
    }

    @Test
    @DisplayName("Does not allow the blue ability to target a sorcery")
    void rejectsSorceryWithBlueAbility() {
        TrainOfThought trainOfThought = new TrainOfThought();
        harness.setHand(player1, List.of(trainOfThought));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addGuildmage();

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, trainOfThought.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow the red ability to target an instant")
    void rejectsInstantWithRedAbility() {
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addGuildmage();

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, pyromatics.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow an instant with mana value greater than two")
    void rejectsHighManaValueInstant() {
        CerebralVortex cerebralVortex = new CerebralVortex();
        harness.setHand(player1, List.of(cerebralVortex));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        addGuildmage();

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, cerebralVortex.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow a sorcery with mana value greater than two")
    void rejectsHighManaValueSorcery() {
        StitchInTime stitchInTime = new StitchInTime();
        harness.setHand(player1, List.of(stitchInTime));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addGuildmage();

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, stitchInTime.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow copying an opponent's spell")
    void rejectsOpponentSpell() {
        TrainOfThought trainOfThought = new TrainOfThought();
        harness.setHand(player2, List.of(trainOfThought));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        addGuildmage();

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, trainOfThought.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining new targets resolves both Pyromatics spells against the original target")
    void decliningNewTargetsPreservesDamageAndOriginalSpell() {
        Pyromatics pyromatics = new Pyromatics();
        harness.setHand(player1, List.of(pyromatics));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        addGuildmage();

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, pyromatics.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(pyromatics);
    }

    @Test
    @DisplayName("An untargeted sorcery copy and its original each draw a card")
    void untargetedSorceryCopyResolvesWithoutRetargeting() {
        TrainOfThought original = new TrainOfThought();
        Pyromatics firstDraw = new Pyromatics();
        CerebralVortex secondDraw = new CerebralVortex();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        addGuildmage();

        harness.castSorcery(player1, 0);
        harness.activateAbility(player1, 0, 1, null, original.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Guildmage can activate twice without tapping")
    void summoningSickGuildmageCanCopyTheSameSpellTwice() {
        TrainOfThought original = new TrainOfThought();
        Pyromatics firstDraw = new Pyromatics();
        Pyromatics secondDraw = new Pyromatics();
        Pyromatics thirdDraw = new Pyromatics();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addToBattlefield(player1, new IzzetGuildmage());

        harness.castSorcery(player1, 0);
        harness.activateAbility(player1, 0, 1, null, original.getId());
        harness.activateAbility(player1, 0, 1, null, original.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addGuildmage() {
        addCreatureReady(player1, new IzzetGuildmage());
    }
}

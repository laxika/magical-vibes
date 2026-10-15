package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Annex.class, Demystify.class, ElvishWarrior.class, Forest.class})
class AnnexTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Annex targeting a land puts it on the stack")
    void castingPutsOnStack() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Resolving Annex targeting your own land leaves it under your control")
    void canEnchantOwnLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(forest.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Annex
                        && p.isAttached()
                        && p.getAttachedTo().equals(forest.getId()));
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Annex steals opponent's land")
    void resolvingStealsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        // Land should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(forest.getId()));

        // Annex aura should be on player1's battlefield attached to the land
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Annex
                        && p.isAttached()
                        && p.getAttachedTo().equals(forest.getId()));

        // Land should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(forest.getId(), player2.getId());
    }

    @Test
    @DisplayName("Annex fizzles if target land is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());

        // Remove the land before resolution
        gd.playerBattlefields.get(player2.getId()).remove(forest);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Annex");
    }

    @Test
    @DisplayName("Land returns to owner when Annex is destroyed")
    void landReturnsWhenAnnexDestroyed() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));

        Permanent annexPerm = findPermanent(player1, "Annex");

        // Destroy the aura with Demystify
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, annexPerm.getId());

        // Land should return to player2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(forest.getId());
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a nonland permanent with Annex")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player2, new Forest()); // valid target so spell is playable
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("A stolen noncreature land can immediately be tapped for mana")
    void stolenLandCanImmediatelyProduceMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Destroying the newer Annex leaves the older Annex controlling the land")
    void olderAnnexStillControlsLandAfterNewerAnnexIsDestroyed() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Annex(), new Annex()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        Permanent olderAnnex = findPermanent(player1, "Annex");
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        Permanent newerAnnex = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof Annex && !p.getId().equals(olderAnnex.getId()))
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new Demystify(), new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, newerAnnex.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest, olderAnnex);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
        assertThat(olderAnnex.getAttachedTo()).isEqualTo(forest.getId());

        harness.castAndResolveInstant(player1, 0, olderAnnex.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
    }
}

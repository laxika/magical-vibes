package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoaringHope.class, GrizzlyBears.class, FountainOfYouth.class})
class SoaringHopeTest extends BaseCardTest {

    // ===== ETB life gain =====

    @Test
    @DisplayName("ETB trigger causes controller to gain 3 life")
    void etbGainsLife() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SoaringHope()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities(); // resolve aura spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new SoaringHope());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Activated ability: put on top of library =====

    @Test
    @DisplayName("Activating {W} ability puts Soaring Hope on top of its owner's library")
    void activateAbilityPutsOnTopOfLibrary() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new SoaringHope());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        // Off the battlefield and not in hand
        harness.assertNotOnBattlefield(player1, "Soaring Hope");
        harness.assertNotInHand(player1, "Soaring Hope");
        // On top of the owner's library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Soaring Hope");
    }

    @Test
    @DisplayName("Controlled Soaring Hope goes to its owner's library")
    void activateAbilityUsesOwnerLibraryWhenControlledByOpponent() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        SoaringHope auraCard = new SoaringHope();
        auraCard.setOwnerId(player1.getId());
        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(auraPerm);

        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(auraCard.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(auraCard.getId()));
    }

    @Test
    @DisplayName("Creature loses flying after Soaring Hope is put on top of library")
    void creatureLosesFlyingAfterTuck() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new SoaringHope());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SoaringHope()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SoaringHope()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soaring Hope");
        harness.assertNotOnBattlefield(player1, "Soaring Hope");
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedemptionArc.class, GrizzlyBears.class, Mountain.class, AuraGraft.class,
        Naturalize.class, Terror.class})
class RedemptionArcTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has indestructible and is goaded")
    void enchantedCreatureHasIndestructibleAndIsGoaded() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachArc(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability exiles the enchanted creature")
    void activatedAbilityExilesEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachArc(player1, creature);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        harness.assertInGraveyard(player1, "Redemption Arc");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RedemptionArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void goadedCreatureCannotSkipAttacking() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachArc(player1, creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void goadedCreatureCanAttackAuraControllerInTwoPlayerGame() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachArc(player1, creature);

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
    }

    @Test
    void indestructibilityPreventsDestruction() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachArc(player1, creature);

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void removingAuraEndsIndestructibilityAndGoad() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachArc(player1, creature);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.isGoaded(gd, creature)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isZero();
    }

    @Test
    void exileAbilityStillResolvesAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachArc(player1, creature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        harness.assertInGraveyard(player1, "Redemption Arc");
    }

    @Test
    void exileAbilityUsesCreatureEnchantedAtResolution() {
        Permanent originalCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachArc(player1, originalCreature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, newCreature.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(newCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalCreature)
                .doesNotContain(newCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(newCreature.getCard())
                .doesNotContain(originalCreature.getCard());
        harness.assertInGraveyard(player1, "Redemption Arc");
    }

    private Permanent attachArc(Player controller, Permanent creature) {
        harness.setHand(controller, List.of(new RedemptionArc()));
        harness.addMana(controller, ManaColor.WHITE, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);
        harness.castEnchantment(controller, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(controller, "Redemption Arc");
    }
}

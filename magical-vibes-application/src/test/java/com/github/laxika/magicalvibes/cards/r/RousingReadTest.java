package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RousingRead.class, GrizzlyBears.class, Island.class, FountainOfYouth.class})
class RousingReadTest extends BaseCardTest {

    @Test
    @DisplayName("When Rousing Read enters, draws two cards then discards one")
    void drawsTwoThenDiscardsOne() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new RousingRead(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RousingRead());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RousingRead()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws and discards for the Aura controller")
    void enchantingOpponentsCreatureUsesAuraController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new RousingRead()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rousing Read");
        assertThat(findPermanent(player1, "Rousing Read").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Aura with a missing target does not enter or trigger its draw")
    void missingTargetPreventsDrawAndDiscard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new RousingRead()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rousing Read");
        harness.assertInGraveyard(player1, "Rousing Read");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The enter trigger resolves even after the Aura leaves the battlefield")
    void drawTriggerSurvivesAuraLeaving() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new RousingRead()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Rousing Read");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Island");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}

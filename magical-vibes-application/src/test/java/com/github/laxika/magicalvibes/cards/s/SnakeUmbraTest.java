package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrimstoneMage;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnakeUmbra.class, GrizzlyBears.class, DoomBlade.class, BrimstoneMage.class})
class SnakeUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Snake Umbra attaches and grants +1/+1")
    void attachesAndBoosts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SnakeUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SnakeUmbra
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature may draw when it deals damage to an opponent")
    void mayDrawOnDamageToOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(bears.getId());
        bears.setSummoningSick(false);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Umbra armor saves the enchanted creature and destroys Snake Umbra")
    void umbraArmorSavesEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Snake Umbra");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void mayDeclineDrawAfterCombatDamage() {
        Permanent creature = addCreatureReady(player1, new BrimstoneMage());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SnakeUmbra()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawBelongsToEnchantedCreaturesController() {
        Permanent creature = addCreatureReady(player2, new BrimstoneMage());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SnakeUmbra()));

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 17);
        harness.assertInHand(player2, "Snake Umbra");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDrawOneCardAfterNoncombatDamageToOpponent() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        mage.setCounterCount(CounterType.LEVEL, 3);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(mage.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SnakeUmbra(), new SnakeUmbra()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void damageToOwnControllerDoesNotTriggerDraw() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        mage.setCounterCount(CounterType.LEVEL, 1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(mage.getId());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void umbraArmorRemovesLethalDamageWithoutTappingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrimstoneMage());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SnakeUmbra());
        aura.setAttachedTo(creature.getId());
        Permanent mage = addCreatureReady(player2, new BrimstoneMage());
        mage.setCounterCount(CounterType.LEVEL, 3);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Snake Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}

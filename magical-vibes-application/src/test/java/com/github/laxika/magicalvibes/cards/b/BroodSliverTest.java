package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodSliver.class, MetallicSliver.class, GrizzlyBears.class})
class BroodSliverTest extends BaseCardTest {

    @Test
    void combatDamageOffersToCreateAColorlessSliverToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Sliver");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SLIVER);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void decliningTheAbilityCreatesNoToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void nonSliverCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void blockedSliverDoesNotTrigger() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player1, new MetallicSliver());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MetallicSliver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }

    @Test
    void controllerOfTheSliverThatDealtDamageChoosesAndGetsTheToken() {
        addCreatureReady(player1, new BroodSliver());
        Permanent attacker = addCreatureReady(player2, new MetallicSliver());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Sliver").getCard().isToken()).isTrue();
        harness.assertNotOnBattlefield(player1, "Sliver");
    }
}

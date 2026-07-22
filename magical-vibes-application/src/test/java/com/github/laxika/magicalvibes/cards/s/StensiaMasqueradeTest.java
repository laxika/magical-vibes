package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodmadVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StensiaMasqueradeTest extends BaseCardTest {

    private Permanent addAttackingCreature(Player controller, Permanent permanent) {
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }

    private StensiaMasquerade discardViaRavensCrime() {
        StensiaMasquerade card = new StensiaMasquerade();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return card;
    }

    @Test
    @DisplayName("Attacking creatures you control have first strike")
    void attackingCreaturesHaveFirstStrike() {
        harness.addToBattlefield(player1, new StensiaMasquerade());
        Permanent bears = addAttackingCreature(player1, new Permanent(new GrizzlyBears()));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures you control do not have first strike")
    void nonAttackingCreaturesDoNotHaveFirstStrike() {
        harness.addToBattlefield(player1, new StensiaMasquerade());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not gain first strike")
    void opponentAttackersDoNotGainFirstStrike() {
        harness.addToBattlefield(player1, new StensiaMasquerade());
        Permanent bears = addAttackingCreature(player2, new Permanent(new GrizzlyBears()));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampire you control gets a +1/+1 counter when dealing combat damage to a player")
    void vampireGetsCounterOnCombatDamage() {
        harness.addToBattlefield(player1, new StensiaMasquerade());
        Permanent vampire = addAttackingCreature(player1, new Permanent(new BloodmadVampire()));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve both triggers (Bloodmad Vampire's own + Stensia Masquerade)
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Vampire does not get a counter from Stensia Masquerade")
    void nonVampireDoesNotTrigger() {
        harness.addToBattlefield(player1, new StensiaMasquerade());
        Permanent bears = addAttackingCreature(player1, new Permanent(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Discarding Stensia Masquerade exiles it and offers madness cast")
    void discardTriggersMadness() {
        StensiaMasquerade card = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(card.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting madness cast pays {2}{R} and puts the enchantment on the battlefield")
    void acceptingMadnessCastsEnchantment() {
        StensiaMasquerade card = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(card.getId()));
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendOfSpite.class, GrizzlyBears.class, Shock.class, Unhinge.class})
class ArchfiendOfSpiteTest extends BaseCardTest {

    private static final String SACRIFICE_TWO = "Sacrifice 2 permanents";
    private static final String LOSE_TWO = "Lose 2 life";

    @Test
    @DisplayName("The damage source's controller may lose life instead of sacrificing permanents")
    void choosesLifeLoss() {
        setupDamageScenario(3);
        int life = gd.getLife(player1.getId());

        resolveDamageTrigger();
        harness.handleListChoice(player1, LOSE_TWO);

        assertThat(gd.getLife(player1.getId())).isEqualTo(life - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing to sacrifice exactly the damage amount sacrifices those permanents")
    void sacrificesExactlyTheDamageAmount() {
        setupDamageScenario(2);
        int life = gd.getLife(player1.getId());

        resolveDamageTrigger();
        harness.handleListChoice(player1, SACRIFICE_TWO);

        assertThat(gd.getLife(player1.getId())).isEqualTo(life);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing to sacrifice with extra permanents opens a multi-permanent choice")
    void choosesWhichPermanentsToSacrifice() {
        setupDamageScenario(3);

        resolveDamageTrigger();
        harness.handleListChoice(player1, SACRIFICE_TWO);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        List<UUID> battlefieldIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getId()).toList();
        harness.handleMultiplePermanentsChosen(player1, battlefieldIds.subList(0, 2));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding Archfiend of Spite offers its madness cost")
    void discardTriggersMadness() {
        ArchfiendOfSpite archfiend = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(archfiend.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(archfiend.getId()));
    }

    @Test
    @DisplayName("Insufficient permanents causes full life loss without sacrificing any")
    void insufficientPermanentsCausesLifeLoss() {
        setupDamageScenario(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Damage from a source controlled by Archfiend's controller does not trigger")
    void ownSourceDoesNotTrigger() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player1, new ArchfiendOfSpite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, archfiend.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Archfiend of Spite");
    }

    @Test
    @DisplayName("Combat damage triggers even when the damage source dies in the same combat")
    void deadCombatSourceStillCausesLifeLoss() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent archfiend = harness.addToBattlefieldAndReturn(player2, new ArchfiendOfSpite());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        archfiend.setBlocking(true);
        archfiend.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Archfiend of Spite");
    }

    @Test
    @DisplayName("Lethal damage still triggers after Archfiend leaves the battlefield")
    void lethalDamageStillTriggers() {
        Permanent archfiend = harness.addToBattlefieldAndReturn(player2, new ArchfiendOfSpite());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, archfiend.getId());
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Archfiend of Spite");
        harness.assertInGraveyard(player2, "Archfiend of Spite");
    }

    @Test
    @DisplayName("Declining madness puts the discarded card into the graveyard")
    void decliningMadnessPutsCardInGraveyard() {
        ArchfiendOfSpite archfiend = discardViaUnhinge();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(archfiend.getId()));
        harness.assertInGraveyard(player1, "Archfiend of Spite");
        harness.assertNotOnBattlefield(player1, "Archfiend of Spite");
    }

    private void setupDamageScenario(int permanentCount) {
        harness.addToBattlefield(player2, new ArchfiendOfSpite());
        for (int i = 0; i < permanentCount; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID archfiendId = harness.getPermanentId(player2, "Archfiend of Spite");
        harness.castAndResolveInstant(player1, 0, archfiendId);
    }

    private void resolveDamageTrigger() {
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private ArchfiendOfSpite discardViaUnhinge() {
        ArchfiendOfSpite archfiend = new ArchfiendOfSpite();
        harness.setHand(player1, List.of(archfiend));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return archfiend;
    }
}

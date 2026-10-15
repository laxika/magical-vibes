package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AutomatedArtificer;
import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.OtawaraSoaringCity;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReplicationSpecialist.class, AutomatedArtificer.class, BambooGroveArcher.class,
        OtawaraSoaringCity.class, MycosynthLattice.class})
class ReplicationSpecialistTest extends BaseCardTest {

    @Test
    void payingTheTriggerCreatesATokenCopyOfTheArtifact() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.setHand(player1, List.of(new AutomatedArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Automated Artificer") && p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    void decliningTheTriggerDoesNotCreateAToken() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.setHand(player1, List.of(new AutomatedArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(1);
    }

    @Test
    void cannotPayTheTriggerWithoutBlueMana() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.setHand(player1, List.of(new AutomatedArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(1);
    }

    @Test
    void tokenCopyDoesNotRetriggerTheAbility() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.setHand(player1, List.of(new AutomatedArtificer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void nonArtifactDoesNotTriggerTheAbility() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.setHand(player1, List.of(new BambooGroveArcher()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void opponentsArtifactDoesNotTriggerTheAbility() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.enterBattlefieldAndReturn(player2, new AutomatedArtificer());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void shroudDoesNotPreventCopyingTheEnteringArtifact() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new AutomatedArtificer());
        artifact.getGrantedKeywords().add(Keyword.SHROUD);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(2);
    }

    @Test
    void copiesTheArtifactAfterItReturnsToItsOwnersHand() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.setHand(player2, List.of(new OtawaraSoaringCity()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Automated Artificer");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Automated Artificer");
                    assertThat(permanent.getCard().isToken()).isTrue();
                });
    }

    @Test
    void triggersForItsOwnEntryWhenItIsAnArtifact() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new ReplicationSpecialist()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Replication Specialist")).isEqualTo(2);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOnlyOneBlueMana() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.enterBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(1);
    }

    @Test
    void eachSpecialistCanCopyTheSameArtifact() {
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.addToBattlefield(player1, new ReplicationSpecialist());
        harness.enterBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void abilityStillCopiesAfterSpecialistLeavesTheBattlefield() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new ReplicationSpecialist());
        harness.enterBattlefieldAndReturn(player1, new AutomatedArtificer());
        harness.setHand(player2, List.of(new OtawaraSoaringCity()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, specialist.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Replication Specialist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Automated Artificer")).isEqualTo(2);
    }

}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlightedAgent;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NumbingDose.class, BlightedAgent.class, PristineTalisman.class})
class NumbingDoseTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Numbing Dose")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target an artifact with Numbing Dose")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Numbing Dose attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Numbing Dose")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Numbing Dose attaches it to target artifact")
    void resolvingAttachesToArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());

        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Numbing Dose")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Tapped creature with Numbing Dose does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());
        creature.tap();

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped artifact with Numbing Dose does not untap during controller's untap step")
    void enchantedArtifactDoesNotUntap() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());
        artifact.tap();

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(artifact.getId());

        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted permanent's controller loses 1 life at their upkeep")
    void enchantedPermanentControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger fires during enchanted artifact controller's upkeep")
    void lifeLossTriggerFiresForArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(artifact.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger does NOT fire during aura controller's upkeep")
    void lifeLossDoesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1 (aura controller) should not lose life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life loss accumulates over multiple upkeeps")
    void lifeLossAccumulatesOverUpkeeps() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Creature can untap again after Numbing Dose is removed")
    void creatureUntapsAfterDoseRemoved() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());
        creature.tap();

        Permanent dosePerm = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dosePerm.setAttachedTo(creature.getId());

        // Remove Numbing Dose
        gd.playerBattlefields.get(player1.getId()).remove(dosePerm);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactEnchantment() {
        Permanent host = addCreatureReady(player2, new BlightedAgent());
        Permanent otherDose = harness.addToBattlefieldAndReturn(player2, new NumbingDose());
        otherDose.setAttachedTo(host.getId());
        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, otherDose.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Numbing Dose does not tap an untapped host when it resolves")
    void resolvingDoesNotTapHost() {
        Permanent creature = addCreatureReady(player2, new BlightedAgent());
        harness.setHand(player1, List.of(new NumbingDose()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Numbing Dose").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An upkeep trigger still causes life loss after the Aura leaves")
    void upkeepTriggerSurvivesAuraRemoval() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());
        Permanent dose = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dose.setAttachedTo(artifact.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(dose);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An Aura on its controller's own creature triggers during that player's upkeep")
    void ownCreatureControllerLosesLife() {
        Permanent creature = addCreatureReady(player1, new BlightedAgent());
        Permanent dose = harness.addToBattlefieldAndReturn(player1, new NumbingDose());
        dose.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}

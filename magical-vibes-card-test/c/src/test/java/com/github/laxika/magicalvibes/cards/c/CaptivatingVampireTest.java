package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptivatingVampire.class, BaronyVampire.class, RuneclawBear.class, Pacifism.class, AmoeboidChangeling.class})
class CaptivatingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Other Vampire creatures you control get +1/+1")
    void buffsOtherOwnVampires() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent barony = findPermanent(player1, "Barony Vampire");

        // Barony Vampire is 3/2 base; with +1/+1 from Captivating Vampire = 4/3
        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barony)).isEqualTo(3);
    }

    @Test
    @DisplayName("Captivating Vampire does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new CaptivatingVampire());

        Permanent captivating = findPermanent(player1, "Captivating Vampire");

        // 2/2 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, captivating)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captivating)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Vampire creatures you control")
    void doesNotBuffNonVampires() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new RuneclawBear());

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Vampires (only 'you control')")
    void doesNotBuffOpponentVampires() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player2, new BaronyVampire());

        Permanent opponentBarony = findPermanent(player2, "Barony Vampire");

        // Should not be buffed (OWN_CREATURES scope)
        assertThat(gqs.getEffectivePower(gd, opponentBarony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBarony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Captivating Vampires buff each other")
    void twoBuffEachOther() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new CaptivatingVampire());

        List<Permanent> captivatingVamps = findPermanents(player1, "Captivating Vampire");

        assertThat(captivatingVamps).hasSize(2);
        for (Permanent vamp : captivatingVamps) {
            assertThat(gqs.getEffectivePower(gd, vamp)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, vamp)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus removed when Captivating Vampire leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new BaronyVampire());

        Permanent barony = findPermanent(player1, "Barony Vampire");
        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Captivating Vampire"));

        assertThat(gqs.getEffectivePower(gd, barony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, barony)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability gains control of target creature and makes it a Vampire")
    void gainControlAndMakeVampire() {
        // Need 5 untapped vampires
        addVampires(player1, 4);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent target = addCreatureReady(player2, new RuneclawBear());

        int captivatingIdx = gd.playerBattlefields.get(player1.getId()).indexOf(captivating);
        harness.activateAbility(player1, captivatingIdx, null, target.getId());

        // Choose 5 vampires to tap (including Captivating Vampire itself)
        List<Permanent> vampires = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE)
                        || p.getGrantedSubtypes().contains(CardSubtype.VAMPIRE))
                .filter(p -> !p.isTapped())
                .limit(5)
                .toList();
        for (Permanent vamp : vampires) {
            harness.handlePermanentChosen(player1, vamp.getId());
        }

        harness.passBothPriorities();

        // Creature should now be controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));

        // Creature should be a Vampire now
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);

        // Control is permanent
        assertThat(gd.newestControlEffectFor(target.getId()).duration()).isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
    }

    @Test
    @DisplayName("Cannot activate ability with fewer than 5 vampires")
    void cannotActivateWithFewerThan5Vampires() {
        // Only 4 vampires (3 + Captivating Vampire)
        addVampires(player1, 3);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent target = addCreatureReady(player2, new RuneclawBear());

        int captivatingIdx = gd.playerBattlefields.get(player1.getId()).indexOf(captivating);

        assertThatThrownBy(() -> harness.activateAbility(player1, captivatingIdx, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addVampires(player1, 4);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        int captivatingIdx = gd.playerBattlefields.get(player1.getId()).indexOf(captivating);

        assertThatThrownBy(() -> harness.activateAbility(player1, captivatingIdx, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Stolen creature becomes a Vampire and receives the lord bonus")
    void stolenCreatureGetsBonusAsVampire() {
        addVampires(player1, 4);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent target = addCreatureReady(player2, new RuneclawBear());

        int captivatingIdx = gd.playerBattlefields.get(player1.getId()).indexOf(captivating);
        harness.activateAbility(player1, captivatingIdx, null, target.getId());

        // Choose 5 vampires to tap
        List<Permanent> vampires = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                .filter(p -> !p.isTapped())
                .limit(5)
                .toList();
        for (Permanent vamp : vampires) {
            harness.handlePermanentChosen(player1, vamp.getId());
        }

        harness.passBothPriorities();

        // Runeclaw Bear (2/2) is now a Vampire and should get +1/+1 from Captivating Vampire
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability does not require tapping self ({T} is not in the cost)")
    void abilityDoesNotRequireSelfTap() {
        addVampires(player1, 5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        source.tap();
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 5, null, target.getId());
        for (Permanent vampire : findPermanents(player1, "Barony Vampire")) {
            if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, vampire.getId());
            }
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Vampires including the source can pay the cost")
    void summoningSickVampiresCanPayCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new BaronyVampire());
        }
        List<Permanent> vampires = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        vampires.forEach(vampire -> vampire.setSummoningSick(true));
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        for (Permanent vampire : vampires) {
            if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, vampire.getId());
            }
        }
        assertThat(vampires).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, source);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BEAR, CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Control and Vampire type persist after the source leaves")
    void controlAndSubtypePersistWithoutSource() {
        addVampires(player1, 4);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        List<Permanent> vampires = List.copyOf(gd.playerBattlefields.get(player1.getId()));

        harness.activateAbility(player1, 4, null, target.getId());
        for (Permanent vampire : vampires) {
            if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, vampire.getId());
            }
        }
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BEAR, CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own creature can be targeted and becomes a Vampire")
    void ownCreatureBecomesVampire() {
        addVampires(player1, 4);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        List<Permanent> vampires = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        Permanent target = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 4, null, target.getId());
        for (Permanent vampire : vampires) {
            if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, vampire.getId());
            }
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target, source);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.BEAR, CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Later Vampire type addition applies after an earlier removal of creature types")
    void vampireTypeAdditionUsesResolutionOrder() {
        addVampires(player1, 4);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        List<Permanent> vampires = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        addCreatureReady(player2, new AmoeboidChangeling());

        harness.activateAbility(player2, 1, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).isEmpty();

        harness.activateAbility(player1, 4, null, target.getId());
        for (Permanent vampire : vampires) {
            if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice) {
                harness.handlePermanentChosen(player1, vampire.getId());
            }
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    private void addVampires(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new BaronyVampire());
        }
    }

}

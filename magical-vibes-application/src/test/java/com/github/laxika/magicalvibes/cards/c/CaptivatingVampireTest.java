package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaptivatingVampireTest extends BaseCardTest {

    // ===== Static effect: +1/+1 to other Vampires you control =====

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
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

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

        List<Permanent> captivatingVamps = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Captivating Vampire"))
                .toList();

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

    // ===== Activated ability: Gain control of target creature =====

    @Test
    @DisplayName("Activated ability gains control of target creature and makes it a Vampire")
    void gainControlAndMakeVampire() {
        // Need 5 untapped vampires
        addVampires(player1, 4);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent target = addReadyCreature(player2);

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
        assertThat(gd.permanentControlStolenCreatures).contains(target.getId());
    }

    @Test
    @DisplayName("Cannot activate ability with fewer than 5 vampires")
    void cannotActivateWithFewerThan5Vampires() {
        // Only 4 vampires (3 + Captivating Vampire)
        addVampires(player1, 3);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        Permanent captivating = findPermanent(player1, "Captivating Vampire");
        captivating.setSummoningSick(false);

        Permanent target = addReadyCreature(player2);

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

        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

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

        Permanent target = addReadyCreature(player2);

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

        // Grizzly Bears (2/2) is now a Vampire and should get +1/+1 from Captivating Vampire
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability does not require tapping self ({T} is not in the cost)")
    void abilityDoesNotRequireSelfTap() {
        CaptivatingVampire card = new CaptivatingVampire();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
    }

    @Test
    @DisplayName("Card has correct effects configured")
    void hasCorrectEffects() {
        CaptivatingVampire card = new CaptivatingVampire();

        // Static boost effect
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        // Activated ability with gain control effect
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(e -> e instanceof GainControlOfTargetPermanentEffect);
    }

    // ===== Helpers =====

    private void addVampires(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent vamp = new Permanent(new BaronyVampire());
            vamp.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(vamp);
        }
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}

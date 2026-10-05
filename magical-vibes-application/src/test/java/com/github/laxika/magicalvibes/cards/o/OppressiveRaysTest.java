package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OppressiveRays.class, RuneclawBear.class, ZuranSpellcaster.class, ElvishMystic.class, WallOfGlare.class})
class OppressiveRaysTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can attack when its controller pays {3}")
    void attacksWhenPaid() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        enchant(creature, player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enchanted creature can block when its controller pays {3}")
    void blocksWhenPaid() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        enchant(blocker, player2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Activated abilities of the enchanted creature cost {3} more")
    void activatedAbilityCostsMore() {
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        enchant(spellcaster, player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The attack tax cannot be paid without {3}")
    void attackTaxRequiresPayment() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        enchant(creature, player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Oppressive Rays taxes blocking with the enchanted creature, not blocking it")
    void blockingTheEnchantedCreatureIsFree() {
        Permanent enchanted = addCreatureReady(player1, new RuneclawBear());
        enchanted.setAttacking(true);
        enchant(enchanted, player2);

        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        prepareDeclareBlockers();

        // player2's pool is empty: reading the BLOCK_WITH tax as BE_BLOCKED_BY would reject this
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(enchanted))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The enchanted blocker requires three mana before it can block")
    void blockingRequiresPayment() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        enchant(blocker, player1);
        harness.addMana(player2, ManaColor.WHITE, 2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("An activated ability cannot be activated without paying the full tax")
    void activatedAbilityRequiresPayment() {
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        enchant(spellcaster, player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spellcaster.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana abilities of the enchanted creature also require the activation tax")
    void manaAbilityRequiresPayment() {
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        enchant(mystic, player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mystic.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A mana ability pays the tax before producing its mana")
    void manaAbilityPaysTax() {
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        enchant(mystic, player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.tapPermanent(player1, 0);

        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One block tax permits the enchanted creature to block multiple attackers")
    void multipleBlocksPayOnlyOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new RuneclawBear());
        Permanent secondAttacker = addCreatureReady(player1, new RuneclawBear());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        enchant(wall, player1);
        harness.addMana(player2, ManaColor.WHITE, 3);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(wall.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Oppressive Rays add their attack taxes together")
    void multipleAurasIncreaseAttackTax() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        enchant(creature, player2);
        enchant(creature, player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Removing the Aura ends its activation tax")
    void activationIsFreeAfterAuraLeaves() {
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        enchant(spellcaster, player2);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(spellcaster.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Oppressive Rays enchants the chosen opposing creature and applies its tax")
    void resolvesOntoTargetCreature() {
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        harness.setHand(player1, List.of(new OppressiveRays()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, spellcaster.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Oppressive Rays").getAttachedTo()).isEqualTo(spellcaster.getId());
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    private void enchant(Permanent creature, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new OppressiveRays());
        aura.setAttachedTo(creature.getId());
    }
}

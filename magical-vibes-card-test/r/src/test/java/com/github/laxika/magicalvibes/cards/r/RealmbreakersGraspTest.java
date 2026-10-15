package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UrnOfGodfire;
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

@CardUsed({RealmbreakersGrasp.class, AlloyMyr.class, BottleGnomes.class, FountainOfYouth.class,
        GrizzlyBears.class, Plains.class, UrnOfGodfire.class})
class RealmbreakersGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Realmbreaker's Grasp can enchant an artifact or creature")
    void canEnchantArtifactOrCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new RealmbreakersGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertAttachedTo(artifact);
    }

    @Test
    @DisplayName("Realmbreaker's Grasp cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new RealmbreakersGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attachTo(creature, player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted permanent cannot activate non-mana abilities")
    void enchantedPermanentCannotActivateNonManaAbilities() {
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        attachTo(gnomes, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted permanent can still activate mana abilities")
    void enchantedPermanentCanActivateManaAbilities() {
        Permanent myr = addCreatureReady(player1, new AlloyMyr());
        attachTo(myr, player2);

        harness.activateAbility(player1, 0, null, null);

        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the Aura restores the artifact's non-mana ability")
    void removingAuraRestoresActivatedAbility() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent grasp = attachTo(fountain, player2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(fountain.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(grasp);
        gd.playerGraveyards.get(player2.getId()).add(grasp.getCard());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Aura resolves attached to a nonartifact creature")
    void canEnchantNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealmbreakersGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertAttachedTo(creature);
    }

    @Test
    @DisplayName("Enchanted noncreature artifact allows mana abilities but blocks other activations")
    void enchantedArtifactAllowsOnlyManaAbilities() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent grasp = attachTo(urn, player2);
        harness.addMana(player1, ManaColor.WHITE, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, grasp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(urn.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(urn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(6);
    }

    private Permanent attachTo(Permanent host, Player controller) {
        Permanent grasp = new Permanent(new RealmbreakersGrasp());
        grasp.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(grasp);
        return grasp;
    }

    private void assertAttachedTo(Permanent host) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof RealmbreakersGrasp
                        && p.isAttached()
                        && p.getAttachedTo().equals(host.getId()));
    }
}

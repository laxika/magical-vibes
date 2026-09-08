package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealmbreakersGrasp.class, AlloyMyr.class, BottleGnomes.class, FountainOfYouth.class,
        GrizzlyBears.class, Plains.class})
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted permanent cannot activate non-mana abilities")
    void enchantedPermanentCannotActivateNonManaAbilities() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        gnomes.setSummoningSick(false);
        attachTo(gnomes, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted permanent can still activate mana abilities")
    void enchantedPermanentCanActivateManaAbilities() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new AlloyMyr());
        myr.setSummoningSick(false);
        attachTo(myr, player2);

        harness.activateAbility(player1, 0, null, null);

        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE))
                .isEqualTo(1);
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

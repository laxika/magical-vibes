package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.r.ReinsOfPower;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalmingLicid.class, YouthfulKnight.class, HornOfGreed.class, ReinsOfPower.class})
class CalmingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature can still block")
    void enchantedCreatureCanBlock() {
        addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        addCreatureReady(player1, new YouthfulKnight());
        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(host.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost reverts the Licid to a creature")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetArtifact() {
        addCreatureReady(player1, new CalmingLicid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HornOfGreed());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles and the Licid stays a creature if the target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, host);
            harness.getPermanentRemovalService().removeOrphanedAuras(gd);
        });
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Ending the effect is immediate and does not use the stack")
    void endPaymentIsImmediate() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(licid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ending the effect restores the host's ability to attack")
    void endPaymentRemovesAttackRestriction() {
        addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(host.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The restored Licid can attach to another creature after untapping")
    void canBecomeAuraAgainAfterEndingEffect() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent firstHost = addCreatureReady(player1, new YouthfulKnight());
        Permanent secondHost = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, firstHost.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        licid.untap();

        harness.activateAbility(player1, 0, null, secondHost.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(secondHost.getId());
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("An attached Licid goes to the graveyard when its host dies")
    void auraDiesWithItsHost() {
        addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, host);
            harness.getPermanentRemovalService().removeOrphanedAuras(gd);
        });

        harness.assertNotOnBattlefield(player1, "Calming Licid");
        harness.assertInGraveyard(player1, "Calming Licid");
    }

    @Test
    @DisplayName("A Licid targeting itself becomes an illegal Aura and goes to the graveyard")
    void targetingItselfPutsLicidInGraveyard() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, licid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Calming Licid");
        harness.assertInGraveyard(player1, "Calming Licid");
    }

    @Test
    @DisplayName("A summoning-sick Licid cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Changing control before resolution does not give the new controller the end payment")
    void newControllerCannotEndAnotherPlayersEffect() {
        Permanent licid = addCreatureReady(player1, new CalmingLicid());
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, host.getId());

        harness.setHand(player2, List.of(new ReinsOfPower()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(licid);
        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        harness.addMana(player2, ManaColor.WHITE, 1);
        int licidIndex = gd.playerBattlefields.get(player2.getId()).indexOf(licid);

        assertThatThrownBy(() -> harness.activateAbility(player2, licidIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
    }

}

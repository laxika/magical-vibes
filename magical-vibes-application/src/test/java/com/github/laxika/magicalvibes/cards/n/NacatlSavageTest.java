package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.c.CourtHomunculus;
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

@CardUsed({NacatlSavage.class, CourtHomunculus.class, BoneSaw.class})
class NacatlSavageTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent savage = addCreatureReady(player1, new NacatlSavage());
        savage.setAttacking(true);

        addCreatureReady(player2, new CourtHomunculus());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent savage = addCreatureReady(player1, new NacatlSavage());
        savage.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NacatlSavage());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        Permanent savage = harness.addToBattlefieldAndReturn(player1, new NacatlSavage());

        savage.resetModifiers();

        Permanent artifactSource = new Permanent(new CourtHomunculus());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, savage, artifactSource)).isTrue();

        Permanent nonArtifactSource = new Permanent(new NacatlSavage());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, savage, nonArtifactSource)).isFalse();
    }

    @Test
    @DisplayName("Can block an artifact creature and prevents its combat damage")
    void preventsDamageWhileBlockingArtifactCreature() {
        Permanent attacker = addCreatureReady(player1, new CourtHomunculus());
        attacker.setAttacking(true);
        Permanent savage = addCreatureReady(player2, new NacatlSavage());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Nacatl Savage");
        assertThat(savage.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Court Homunculus");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Combat damage from non-artifact creatures is not prevented")
    void takesDamageFromNonArtifactCreature() {
        Permanent attacker = addCreatureReady(player1, new NacatlSavage());
        attacker.setAttacking(true);
        addCreatureReady(player2, new NacatlSavage());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Nacatl Savage");
        harness.assertInGraveyard(player2, "Nacatl Savage");
    }

    @Test
    @DisplayName("Artifact equip ability cannot target Nacatl Savage")
    void cannotBeTargetedByEquipAbility() {
        harness.addToBattlefield(player1, new BoneSaw());
        Permanent savage = addCreatureReady(player1, new NacatlSavage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, savage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifact Equipment becomes unattached during state-based actions")
    void cannotRemainEquipped() {
        Permanent savage = addCreatureReady(player1, new NacatlSavage());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        equipment.setAttachedTo(savage.getId());

        harness.runStateBasedActions();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Bone Saw");
        harness.assertOnBattlefield(player1, "Nacatl Savage");
    }
}

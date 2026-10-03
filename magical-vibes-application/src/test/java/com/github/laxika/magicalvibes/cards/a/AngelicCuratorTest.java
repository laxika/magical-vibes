package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.r.RingOfGix;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AngelicCurator.class, GrizzlyBears.class, IronMyr.class, RingOfGix.class, Bonesplitter.class})
class AngelicCuratorTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from artifacts prevents blocking by an artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent curator = addCreatureReady(player1, new AngelicCurator());
        curator.setAttacking(true);

        Permanent ironMyr = addCreatureReady(player2, new IronMyr());
        ironMyr.getGrantedKeywords().add(Keyword.FLYING);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by a non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent curator = addCreatureReady(player1, new AngelicCurator());
        curator.setAttacking(true);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.getGrantedKeywords().add(Keyword.FLYING);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        curator.resetModifiers();

        Permanent artifactSource = new Permanent(new IronMyr());
        Permanent nonArtifactSource = new Permanent(new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, curator, artifactSource)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, curator, nonArtifactSource)).isFalse();
    }

    @Test
    @DisplayName("Flying prevents blocking by a ground creature")
    void groundCreatureCannotBlock() {
        Permanent curator = addCreatureReady(player1, new AngelicCurator());
        curator.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Curator can block an artifact creature and prevents its combat damage")
    void preventsArtifactCombatDamageWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new IronMyr());
        attacker.setAttacking(true);
        Permanent curator = addCreatureReady(player2, new AngelicCurator());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Angelic Curator");
        harness.assertInGraveyard(player1, "Iron Myr");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An artifact's activated ability cannot target Curator")
    void artifactAbilityCannotTarget() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfGix());
        Permanent curator = harness.addToBattlefieldAndReturn(player2, new AngelicCurator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, curator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(ring.isTapped()).isFalse();
        assertThat(curator.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Curator cannot be targeted by its controller's equip ability")
    void cannotBeEquipped() {
        harness.addToBattlefield(player1, new Bonesplitter());
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, curator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("State-based actions detach artifact Equipment without destroying it")
    void detachesArtifactEquipment() {
        Permanent curator = harness.addToBattlefieldAndReturn(player1, new AngelicCurator());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(curator.getId());

        harness.runStateBasedActions();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player1, "Angelic Curator");
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlazingFiresingerSeethingSong;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkycoachWaypoint.class, BlazingFiresingerSeethingSong.class, GrizzlyBears.class, BonecrusherGiant.class})
class SkycoachWaypointTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T} prepares target creature with a prepare spell")
    void activatedAbilityPreparesTargetCreature() {
        Permanent waypoint = harness.addToBattlefieldAndReturn(player1, new SkycoachWaypoint());
        Permanent firesinger = harness.addToBattlefieldAndReturn(player1, new BlazingFiresingerSeethingSong());
        assertThat(firesinger.isPrepared()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, firesinger.getId());
        harness.passBothPriorities();

        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(firesinger.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.exilePlayPermissions.get(firesinger.getPreparedSpellCardId()))
                .isEqualTo(player1.getId());
        assertThat(waypoint.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prepare ability has no effect on a creature without a prepare spell")
    void noEffectOnCreatureWithoutPrepareSpell() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isPrepared()).isFalse();
        assertThat(bears.getPreparedSpellCardId()).isNull();
    }

    @Test
    void manaAbilityAddsColorlessImmediately() {
        Permanent waypoint = harness.addToBattlefieldAndReturn(player1, new SkycoachWaypoint());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(waypoint.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preparesOpponentsCreatureForItsController() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        Permanent firesinger = harness.addToBattlefieldAndReturn(player2, new BlazingFiresingerSeethingSong());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, firesinger.getId());
        assertThat(firesinger.isPrepared()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(gd.exilePlayPermissions.get(firesinger.getPreparedSpellCardId())).isEqualTo(player2.getId());
    }

    @Test
    void alreadyPreparedCreatureDoesNotReceiveAnotherSpellCopy() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        Permanent firesinger = harness.enterBattlefieldAndReturn(player1, new BlazingFiresingerSeethingSong());
        assertThat(firesinger.isPrepared()).isTrue();
        UUID originalCopyId = firesinger.getPreparedSpellCardId();
        int permissionCount = gd.exilePlayPermissions.size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, firesinger.getId());
        harness.passBothPriorities();

        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(firesinger.getPreparedSpellCardId()).isEqualTo(originalCopyId);
        assertThat(gd.exilePlayPermissions).hasSize(permissionCount);
    }

    @Test
    void prepareAbilityRejectsNoncreatureTarget() {
        Permanent waypoint = harness.addToBattlefieldAndReturn(player1, new SkycoachWaypoint());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, waypoint.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prepareAbilityRequiresThreeMana() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        Permanent firesinger = harness.addToBattlefieldAndReturn(player1, new BlazingFiresingerSeethingSong());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, firesinger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(firesinger.isPrepared()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureSpellDoesNotCountAsPrepareSpell() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BonecrusherGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int permissionCount = gd.exilePlayPermissions.size();

        harness.activateAbility(player1, 0, 1, null, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.isPrepared()).isFalse();
        assertThat(giant.getPreparedSpellCardId()).isNull();
        assertThat(gd.exilePlayPermissions).hasSize(permissionCount);
    }
}

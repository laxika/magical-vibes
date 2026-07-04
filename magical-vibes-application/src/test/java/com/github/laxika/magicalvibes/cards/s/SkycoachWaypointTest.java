package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlazingFiresingerSeethingSong;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturePreparedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkycoachWaypointTest extends BaseCardTest {

    @Test
    @DisplayName("Has mana ability and activated prepare ability")
    void hasCorrectAbilities() {
        SkycoachWaypoint card = new SkycoachWaypoint();

        assertThat(card.getActivatedAbilities()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AwardManaEffect.class);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(MakeTargetCreaturePreparedEffect.class);
    }

    @Test
    @DisplayName("{3}, {T} prepares target creature with a prepare spell")
    void activatedAbilityPreparesTargetCreature() {
        harness.addToBattlefield(player1, new SkycoachWaypoint());
        harness.addToBattlefield(player1, new BlazingFiresingerSeethingSong());
        Permanent waypoint = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent firesinger = gd.playerBattlefields.get(player1.getId()).get(1);
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isPrepared()).isFalse();
        assertThat(bears.getPreparedSpellCardId()).isNull();
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlareOfSubdual.class, GrizzlyBears.class, AngelsFeather.class, Forest.class})
class GlareOfSubdualTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureByTappingAnUntappedCreatureYouControl() {
        Permanent glare = addPermanent(player1, new GlareOfSubdual());
        Permanent costCreature = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, battlefieldIndex(player1, glare), null, target.getId());

        assertThat(costCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsTargetArtifact() {
        Permanent glare = addPermanent(player1, new GlareOfSubdual());
        addReadyCreature(player1);
        Permanent target = addPermanent(player2, new AngelsFeather());

        harness.activateAbility(player1, battlefieldIndex(player1, glare), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutAnUntappedCreatureToTap() {
        Permanent glare = addPermanent(player1, new GlareOfSubdual());
        Permanent costCreature = addReadyCreature(player1);
        costCreature.tap();
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, glare), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALand() {
        Permanent glare = addPermanent(player1, new GlareOfSubdual());
        addReadyCreature(player1);
        Permanent target = addPermanent(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, glare), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addPermanent(Player player, Card card) {
        return addReadyPermanent(player, card);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

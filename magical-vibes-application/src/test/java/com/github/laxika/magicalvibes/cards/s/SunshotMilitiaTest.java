package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunshotMilitia.class, MoxOpal.class, GrizzlyBears.class, Forest.class})
class SunshotMilitiaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the source and an artifact deals 1 damage to each opponent")
    void tapsArtifactAndDamagesEachOpponent() {
        Permanent militia = addReady(player1, new SunshotMilitia());
        Permanent artifact = addReady(player1, new MoxOpal());

        activate(militia);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(militia.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping the source and a creature satisfies the artifact-or-creature cost")
    void tapsCreatureAndDamagesOpponent() {
        Permanent militia = addReady(player1, new SunshotMilitia());
        Permanent creature = addReady(player1, new GrizzlyBears());

        activate(militia);

        harness.assertLife(player2, 19);
        assertThat(militia.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two qualifying untapped permanents")
    void cannotActivateWithoutTwoQualifyingPermanents() {
        Permanent militia = addReady(player1, new SunshotMilitia());
        Permanent land = addReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        assertThat(militia.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent militia = addReady(player1, new SunshotMilitia());
        Permanent artifact = addReady(player1, new MoxOpal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(militia.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activate(Permanent militia) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(militia), null, null);
        harness.passBothPriorities();
    }
}

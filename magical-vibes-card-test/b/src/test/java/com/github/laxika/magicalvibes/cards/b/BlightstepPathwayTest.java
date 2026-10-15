package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SearstepPathway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightstepPathway.class, SearstepPathway.class})
class BlightstepPathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesBlackMana() {
        harness.setHand(player1, List.of(new BlightstepPathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mana.get(ManaColor.RED)).isZero();
    }

    @Test
    void playingBackFaceProducesRedMana() {
        harness.setHand(player1, List.of(new BlightstepPathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLACK)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndPaysTapCostForImmediateMana(int face) {
        harness.setHand(player1, List.of(new BlightstepPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor produced = face == 0 ? ManaColor.BLACK : ManaColor.RED;
        ManaColor other = face == 0 ? ManaColor.RED : ManaColor.BLACK;
        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(produced)).isEqualTo(1);
        assertThat(mana.get(other)).isZero();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mana.get(produced)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceUsesTheNormalLandPlayForTheTurn(int face) {
        harness.setHand(player1, List.of(new BlightstepPathway(), new BlightstepPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1 - face, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

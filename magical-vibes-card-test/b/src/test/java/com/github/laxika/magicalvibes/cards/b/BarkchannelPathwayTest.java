package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TidechannelPathway;
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

@CardUsed({BarkchannelPathway.class, TidechannelPathway.class})
class BarkchannelPathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesGreenMana() {
        harness.setHand(player1, List.of(new BarkchannelPathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void playingBackFaceProducesBlueMana() {
        harness.setHand(player1, List.of(new BarkchannelPathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.GREEN)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndPaysTapCostForImmediateMana(int face) {
        harness.setHand(player1, List.of(new BarkchannelPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor produced = face == 0 ? ManaColor.GREEN : ManaColor.BLUE;
        ManaColor other = face == 0 ? ManaColor.BLUE : ManaColor.GREEN;
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
        harness.setHand(player1, List.of(new BarkchannelPathway(), new BarkchannelPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1 - face, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

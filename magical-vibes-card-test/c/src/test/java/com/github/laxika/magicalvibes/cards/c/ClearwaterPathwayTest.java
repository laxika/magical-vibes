package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MurkwaterPathway;
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

@CardUsed({ClearwaterPathway.class, MurkwaterPathway.class})
class ClearwaterPathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesBlueMana() {
        harness.setHand(player1, List.of(new ClearwaterPathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLACK)).isZero();
    }

    @Test
    void playingBackFaceProducesBlackMana() {
        harness.setHand(player1, List.of(new ClearwaterPathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLUE)).isZero();
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndManaAbilityPaysTapCostImmediately(int face) {
        harness.setHand(player1, List.of(new ClearwaterPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor expectedColor = face == 0 ? ManaColor.BLUE : ManaColor.BLACK;
        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(expectedColor)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mana.get(expectedColor)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceUsesTheNormalLandPlayForTheTurn(int face) {
        harness.setHand(player1, List.of(new ClearwaterPathway(), new ClearwaterPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1 - face, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

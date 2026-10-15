package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.PillarvergePathway;
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

@CardUsed({NeedlevergePathway.class, PillarvergePathway.class})
class NeedlevergePathwayTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndCanProduceManaImmediately(int face) {
        harness.setHand(player1, List.of(new NeedlevergePathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor chosenColor = face == 0 ? ManaColor.RED : ManaColor.WHITE;
        ManaColor otherColor = face == 0 ? ManaColor.WHITE : ManaColor.RED;
        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(land.isTapped()).isTrue();
        assertThat(mana.get(chosenColor)).isEqualTo(1);
        assertThat(mana.get(otherColor)).isZero();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mana.get(chosenColor)).isEqualTo(1);
        assertThat(mana.get(otherColor)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceConsumesTheLandPlayForTheTurn(int face) {
        harness.setHand(player1, List.of(new NeedlevergePathway(), new NeedlevergePathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1 - face, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playingFrontFaceProducesRedMana() {
        harness.setHand(player1, List.of(new NeedlevergePathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
        assertThat(mana.get(ManaColor.WHITE)).isZero();
    }

    @Test
    void playingBackFaceProducesWhiteMana() {
        harness.setHand(player1, List.of(new NeedlevergePathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.RED)).isZero();
    }
}

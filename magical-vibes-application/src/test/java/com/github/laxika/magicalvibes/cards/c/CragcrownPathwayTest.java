package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TimbercrownPathway;
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

@CardUsed({CragcrownPathway.class, TimbercrownPathway.class})
class CragcrownPathwayTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndPaysTapCostForImmediateMana(int face) {
        harness.setHand(player1, List.of(new CragcrownPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(face == 0 ? 1 : 0);
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(face == 1 ? 1 : 0);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mana.get(ManaColor.RED)).isEqualTo(face == 0 ? 1 : 0);
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(face == 1 ? 1 : 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceUsesTheNormalLandPlay(int face) {
        harness.setHand(player1, List.of(new CragcrownPathway(), new CragcrownPathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void playingFrontFaceProducesRedMana() {
        harness.setHand(player1, List.of(new CragcrownPathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
        assertThat(mana.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void playingBackFaceProducesGreenMana() {
        harness.setHand(player1, List.of(new CragcrownPathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mana.get(ManaColor.RED)).isZero();
    }
}

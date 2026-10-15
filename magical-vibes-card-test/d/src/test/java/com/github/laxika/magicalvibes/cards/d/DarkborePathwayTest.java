package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DarkborePathway.class})
class DarkborePathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesBlackMana() {
        harness.setHand(player1, List.of(new DarkborePathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mana.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void playingBackFaceProducesGreenMana() {
        harness.setHand(player1, List.of(new DarkborePathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLACK)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndManaAbilityPaysTapCost(int face) {
        harness.setHand(player1, List.of(new DarkborePathway()));

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor produced = face == 0 ? ManaColor.BLACK : ManaColor.GREEN;
        ManaColor other = face == 0 ? ManaColor.GREEN : ManaColor.BLACK;
        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(land.isTapped()).isTrue();
        assertThat(mana.get(produced)).isEqualTo(1);
        assertThat(mana.get(other)).isZero();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mana.get(produced)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceUsesTheLandPlayForTheTurn(int face) {
        harness.setHand(player1, List.of(new DarkborePathway(), new DarkborePathway()));

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, face, null, null);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

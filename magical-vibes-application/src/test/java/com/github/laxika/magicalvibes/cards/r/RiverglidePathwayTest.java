package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LavaglidePathway;
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

@CardUsed({RiverglidePathway.class, LavaglidePathway.class})
class RiverglidePathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesBlueMana() {
        harness.setHand(player1, List.of(new RiverglidePathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.RED)).isZero();
    }

    @Test
    void playingBackFaceProducesRedMana() {
        harness.setHand(player1, List.of(new RiverglidePathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.RED)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLUE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceEntersUntappedAndItsManaAbilityResolvesImmediately(int face) {
        harness.setHand(player1, List.of(new RiverglidePathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaColor color = face == 0 ? ManaColor.BLUE : ManaColor.RED;
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void eitherFaceUsesTheNormalLandPlayAllowance(int face) {
        harness.setHand(player1, List.of(new RiverglidePathway(), new RiverglidePathway()));

        gs.playCard(gd, player1, 0, face, null, null);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1 - face, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}

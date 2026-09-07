package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SparkHarvest.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class})
class SparkHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and destroys target creature")
    void sacrificesCreatureAndDestroysTarget() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(sacrifice.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(sacrifice.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Pays {3}{B} instead of sacrificing and destroys target")
    void paysManaInsteadOfSacrificing() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(target.getCard().getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Destroys a target planeswalker when paying the mana option")
    void destroysPlaneswalkerPayingMana() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, planeswalker.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(planeswalker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(planeswalker.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the alternate cost")
    void cannotCastWithoutCreatureOrMana() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Rejects a non-creature, non-planeswalker target")
    void rejectsLandTarget() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent land = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, landId, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = new Permanent(new GarrukWildspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

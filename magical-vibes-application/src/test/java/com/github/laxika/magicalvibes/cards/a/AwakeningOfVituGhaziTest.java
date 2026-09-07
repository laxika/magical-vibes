package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AwakeningOfVituGhazi.class, Forest.class, GrizzlyBears.class})
class AwakeningOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Puts nine counters on a land and permanently makes it Vitu-Ghazi")
    void animatesTargetLand() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new AwakeningOfVituGhazi()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(9);
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.getEffectiveName(gd, land)).isEqualTo("Vitu-Ghazi");
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Name and legendary status survive end-of-turn cleanup")
    void persistentCharacteristicsSurviveCleanup() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new AwakeningOfVituGhazi()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        land.resetModifiers();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectiveName(gd, land)).isEqualTo("Vitu-Ghazi");
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Can target only a land you control")
    void restrictsTargetsToControlledLands() {
        Permanent opponentLand = addLand(player2);
        harness.setHand(player1, List.of(new AwakeningOfVituGhazi()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creature);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(Player player) {
        Permanent land = new Permanent(new Forest());
        land.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}

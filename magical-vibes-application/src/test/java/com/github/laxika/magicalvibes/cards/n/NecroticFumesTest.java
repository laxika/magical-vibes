package com.github.laxika.magicalvibes.cards.n;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecroticFumes.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class})
class NecroticFumesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature as an additional cost and exiles the target creature")
    void exilesCreatureAsCostAndTarget() {
        Permanent costCreature = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(costCreature);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), costCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(costCreature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void exilesTargetPlaneswalker() {
        Permanent costCreature = new Permanent(new GrizzlyBears());
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        gd.playerBattlefields.get(player1.getId()).add(costCreature);

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, planeswalker.getId(), costCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(planeswalker.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals(planeswalker.getCard().getName()));
    }

    @Test
    @DisplayName("Rejects a non-creature as the additional cost")
    void rejectsNonCreatureCost() {
        Permanent land = new Permanent(new Plains());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(land);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Rejects a land as the target")
    void rejectsLandTarget() {
        Permanent costCreature = new Permanent(new GrizzlyBears());
        Permanent land = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(costCreature);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId(), costCreature.getId()))
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

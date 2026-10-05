package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.ProfessorOnyx;
import com.github.laxika.magicalvibes.cards.b.BayouGroff;
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

@CardUsed({NecroticFumes.class, ProfessorOnyx.class, BayouGroff.class, Plains.class})
class NecroticFumesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature as an additional cost and exiles the target creature")
    void exilesCreatureAsCostAndTarget() {
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayouGroff());

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), costCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(costCreature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Bayou Groff"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Bayou Groff"));
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void exilesTargetPlaneswalker() {
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        Permanent planeswalker = addReadyPlaneswalker(player2, 5);

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
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BayouGroff());

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
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId(), costCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile the targeted creature to pay the cost")
    void canPayCostWithTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature.getCard());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof NecroticFumes);
    }

    @Test
    @DisplayName("Can target another creature you control")
    void canExileOwnCreature() {
        Permanent cost = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BayouGroff());
        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), cost.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(cost.getCard(), target.getCard());
    }

    @Test
    @DisplayName("Cannot exile an opponent's creature as the additional cost")
    void rejectsOpponentCreatureCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BayouGroff());
        harness.setHand(player1, List.of(new NecroticFumes()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ProfessorOnyx());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}

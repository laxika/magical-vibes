package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EatenAlive.class, GrizzlyBears.class, GarrukWildspeaker.class, Plains.class})
class EatenAliveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and exiles target creature")
    void sacrificesCreatureAndExilesTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Pays {3}{B} instead of sacrificing and exiles target")
    void paysManaInsteadOfSacrificing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(0);
    }

    @Test
    @DisplayName("Exiles target planeswalker when paying the mana option")
    void exilesPlaneswalkerPayingMana() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, planeswalker.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(planeswalker.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals(planeswalker.getCard().getName()));
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the alternate cost")
    void cannotCastWithoutCreatureOrMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Rejects non-creature, non-planeswalker targets")
    void rejectsLandTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, landId, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice the targeted creature; the cost is not refunded")
    void sacrificesItsOwnTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eaten Alive");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The mana option requires two black mana in the total cost")
    void manaOptionRequiresTwoBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EatenAlive()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}

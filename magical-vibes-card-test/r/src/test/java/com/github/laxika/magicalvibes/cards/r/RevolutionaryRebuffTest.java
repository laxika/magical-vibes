package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.t.TerrainElemental;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevolutionaryRebuff.class, TerrainElemental.class, ConsulateSkygate.class})
class RevolutionaryRebuffTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a nonartifact spell when its controller cannot pay {2}")
    void countersNonartifactSpellWhenControllerCannotPay() {
        TerrainElemental elemental = new TerrainElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new RevolutionaryRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Terrain Elemental");
        harness.assertNotOnBattlefield(player1, "Terrain Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaves a nonartifact spell on the stack when its controller pays {2}")
    void nonartifactSpellResolvesWhenControllerPays() {
        TerrainElemental elemental = new TerrainElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new RevolutionaryRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Terrain Elemental");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counters a nonartifact spell when its controller declines to pay {2}")
    void countersNonartifactSpellWhenControllerDeclinesToPay() {
        TerrainElemental elemental = new TerrainElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new RevolutionaryRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Terrain Elemental");
        harness.assertNotOnBattlefield(player1, "Terrain Elemental");
    }

    @Test
    @DisplayName("Cannot target an artifact spell")
    void cannotTargetArtifactSpell() {
        ConsulateSkygate skygate = new ConsulateSkygate();
        harness.setHand(player1, List.of(skygate));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new RevolutionaryRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, skygate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact spell");
    }

    @Test
    @DisplayName("Counters when only one mana remains without taking a partial payment")
    void countersWhenControllerHasOnlyOneMana() {
        TerrainElemental elemental = new TerrainElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new RevolutionaryRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        harness.assertInGraveyard(player1, "Terrain Elemental");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter a nonartifact instant spell")
    void countersNonartifactInstant() {
        TerrainElemental elemental = new TerrainElemental();
        RevolutionaryRebuff opposingRebuff = new RevolutionaryRebuff();
        harness.setHand(player1, List.of(elemental, new RevolutionaryRebuff()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(opposingRebuff));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingRebuff.getId());

        harness.assertInGraveyard(player2, "Revolutionary Rebuff");
        harness.assertNotInGraveyard(player1, "Terrain Elemental");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Terrain Elemental");
    }
}

package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfProtectionBlack.class, Corrupt.class, GrizzlyBears.class, Mountain.class, Swamp.class})
class CorruptTest extends BaseCardTest {

    @Test
    @DisplayName("Corrupt targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Corrupt.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Corrupt deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // 3 damage kills Grizzly Bears (2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Controller gains 3 life (equal to Swamp count)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Corrupt deals damage to player equal to Swamps controlled and gains life")
    void dealsDamageToPlayerAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        // Player 2 takes 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Player 1 gains 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Corrupt counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Only 1 damage (1 Swamp controlled by player1), Grizzly Bears survives (2 toughness)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        // Controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Corrupt counts Swamps at resolution time")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());

        // Remove all Swamps before resolution
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Swamp"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 0 Swamps at resolution, so 0 damage and 0 life gain
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Corrupt fizzles when target creature is removed before resolution — no life gain")
    void fizzlesWhenTargetCreatureRemoved() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell fizzles — no damage to the opponent and no life gain despite controlling 2 Swamps
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Corrupt does not count non-Swamp lands")
    void doesNotCountNonSwampLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        // Only 1 Swamp, so 1 damage and 1 life gained
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Corrupt gains life only for damage actually dealt")
    void gainsLifeOnlyForDamageActuallyDealt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new CircleOfProtectionBlack());
        Corrupt corrupt = new Corrupt();
        harness.setHand(player1, List.of(corrupt));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, corrupt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}

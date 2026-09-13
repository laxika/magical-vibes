package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.e.EnergyField;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({Corrupt.class, ArgothianSwine.class, Plains.class, Swamp.class})
class CorruptTest extends BaseCardTest {

    @Test
    @DisplayName("Corrupt targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Swine");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Corrupt deals damage to creature equal to Swamps controlled and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Swine");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // 3 damage kills Argothian Swine (3 toughness)
        harness.assertNotOnBattlefield(player2, "Argothian Swine");
        harness.assertInGraveyard(player2, "Argothian Swine");
        // Controller gains 3 life (equal to Swamp count)
        harness.assertLife(player1, 23);
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

        // Player 2 takes 4 damage
        harness.assertLife(player2, 16);
        // Player 1 gains 4 life
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Corrupt counts only controller's Swamps, not opponent's")
    void countsOnlyControllerSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Swine");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Only 1 damage (1 Swamp controlled by player1), Argothian Swine survives (3 toughness)
        harness.assertOnBattlefield(player2, "Argothian Swine");
        // Controller gains 1 life
        harness.assertLife(player1, 21);
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

        // 0 Swamps at resolution, so 0 damage and 0 life gain
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Corrupt fizzles when target creature is removed before resolution — no life gain")
    void fizzlesWhenTargetCreatureRemoved() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Argothian Swine");
        harness.castSorcery(player1, 0, targetId);

        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no damage to the opponent and no life gain despite controlling 2 Swamps
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Corrupt does not count non-Swamp lands")
    void doesNotCountNonSwampLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only 1 Swamp, so 1 damage and 1 life gained
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @CardUsed(EnergyField.class)
    @DisplayName("Corrupt gains life only for damage actually dealt when damage is prevented")
    void preventedDamageDoesNotGrantLife() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new EnergyField());
        harness.setHand(player1, List.of(new Corrupt()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}

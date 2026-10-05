package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.v.VertigoSpawn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeTheFiremind.class, VertigoSpawn.class, IzzetSignet.class})
class InvokeTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards in draw mode")
    void drawsXCards() {
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        harness.setLibrary(player1, List.of(
                new VertigoSpawn(), new VertigoSpawn(), new VertigoSpawn(), new VertigoSpawn()));
        addMana(3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals X damage to any target in damage mode")
    void dealsXDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VertigoSpawn());
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        addMana(1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 1, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals X damage to a player in damage mode")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        int lifeBefore = gd.getLife(player2.getId());
        addMana(2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 2, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Damage mode cannot target an artifact")
    void damageModeRejectsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        addMana(1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 1, new int[]{1}, 1, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw mode with X zero draws nothing from an empty library")
    void zeroDrawDoesNotAttemptToDraw() {
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        harness.setLibrary(player1, List.of());
        addMana(0);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Invoke the Firemind");
        assertThat(gd.playersAttemptedDrawFromEmptyLibrary).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Damage mode with X zero deals no damage")
    void zeroDamageDoesNotChangeLife() {
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        int lifeBefore = gd.getLife(player2.getId());
        addMana(0);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        harness.assertInGraveyard(player1, "Invoke the Firemind");
    }

    @Test
    @DisplayName("Damage mode can deal lethal damage to a creature you control")
    void damageModeCanKillOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VertigoSpawn());
        harness.setHand(player1, List.of(new InvokeTheFiremind()));
        addMana(3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{1}, 3, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vertigo Spawn");
        harness.assertInGraveyard(player1, "Vertigo Spawn");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}

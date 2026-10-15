package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Molder.class, HowlingMine.class, GhostlyPrison.class, GrizzlyBears.class})
class MolderTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact with mana value X and you gain X life")
    void destroysArtifactAndGainsLife() {
        harness.setLife(player1, 15);
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Molder()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Destroys an artifact even when it has a regeneration shield")
    void cannotBeRegenerated() {
        harness.setLife(player1, 15);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Molder()));
        addMana(2);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Destroys an enchantment with mana value X")
    void destroysEnchantmentWithMatchingManaValue() {
        harness.addToBattlefield(player2, new GhostlyPrison());
        harness.setHand(player1, List.of(new Molder()));
        addMana(3);

        UUID targetId = harness.getPermanentId(player2, "Ghostly Prison");
        harness.castInstant(player1, 0, 3, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghostly Prison");
    }

    @Test
    @DisplayName("Cannot target an artifact with a different mana value")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Molder()));
        addMana(3);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 3, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Molder()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No life is gained when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeaves() {
        harness.setLife(player1, 15);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Molder()));
        addMana(2);

        harness.castInstant(player1, 0, 2, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Molder");
    }

    @Test
    @DisplayName("Can destroy your own artifact and gain life")
    void canDestroyOwnArtifact() {
        harness.setLife(player1, 15);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Molder()));
        addMana(2);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}

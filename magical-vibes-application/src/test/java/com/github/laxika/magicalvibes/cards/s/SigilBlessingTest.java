package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigilBlessing.class, CylianElf.class, ObeliskOfBant.class, ResoundingWave.class})
class SigilBlessingTest extends BaseCardTest {

    private void addManaCost(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Target creature you control gets +3/+3, other own creatures get +1/+1")
    void boostsTargetAndOthers() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        List<Permanent> bears = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID targetId = bears.getFirst().getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = bears.getFirst();
        Permanent other = bears.get(1);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent opponentBear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        List<Permanent> bears = harness.getGameData().playerBattlefields.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, bears.getFirst().getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(bears.getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.get(1).getEffectivePower()).isEqualTo(2);
        assertThat(bears.get(1).getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new CylianElf()); // legal target so the spell is castable
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        UUID opponentBearId = harness.getPermanentId(player2, "Cylian Elf");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
    @Test
    @DisplayName("No creatures are boosted when the only target leaves before resolution")
    void doesNotBoostOthersWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        harness.setHand(player2, List.of(new ResoundingWave()));
        addManaCost(player1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SigilBlessing);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new CylianElf());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the other-creature boost")
    void boostsCreaturesEnteringBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);
        harness.castInstant(player1, 0, target.getId());

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new CylianElf());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        assertThat(newcomer.getEffectivePower()).isEqualTo(3);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetOwnNoncreature() {
        harness.addToBattlefield(player1, new CylianElf());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());
        harness.setHand(player1, List.of(new SigilBlessing()));
        addManaCost(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

class FaithUnbrokenTest extends BaseCardTest {

    private void castAndResolve(UUID enchantTargetId, UUID exileTargetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FaithUnbroken()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, List.of(enchantTargetId, exileTargetId));
        harness.passBothPriorities(); // resolve aura → attach + ETB on stack
        harness.passBothPriorities(); // resolve ETB → exile
    }

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);
        Permanent opponent = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponent);

        castAndResolve(mine.getId(), opponent.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponent.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Faith Unbroken leaves the battlefield")
    void exiledReturnsWhenAuraLeaves() {
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);
        Permanent opponent = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponent);

        castAndResolve(mine.getId(), opponent.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraId = harness.getPermanentId(player1, "Faith Unbroken");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureBoosted() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FaithUnbroken());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature loses +2/+2 when Faith Unbroken is removed")
    void boostStopsWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FaithUnbroken());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);
        Permanent anotherOpponent = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(anotherOpponent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FaithUnbroken()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(opponentBears.getId(), anotherOpponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot exile a creature you control")
    void cannotExileOwnCreature() {
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);
        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(other);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FaithUnbroken()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(mine.getId(), other.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }
}

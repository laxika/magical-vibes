package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AshenmoorGouger;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildSwing.class, AshenmoorGouger.class, GreaterAuramancy.class, WoodedBastion.class})
class WildSwingTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys exactly one of the three targets at random")
    void destroysOneOfThree() {
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<Permanent> enemy = gd.playerBattlefields.get(player2.getId());
        List<UUID> targets = List.of(enemy.get(0).getId(), enemy.get(1).getId(), enemy.get(2).getId());

        harness.castAndResolveSorcery(player1, 0, targets);

        // One at random dies, the other two remain.
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent auramancy = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<Permanent> enemy = gd.playerBattlefields.get(player2.getId());
        List<UUID> targets = List.of(auramancy.getId(), enemy.get(0).getId(), enemy.get(1).getId());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a noncreature nonenchantment permanent")
    void canTargetNoncreatureNonenchantmentPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WoodedBastion());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<UUID> targets = List.of(land.getId(), creature.getId(), otherCreature.getId());

        harness.castAndResolveSorcery(player1, 0, targets);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target the same permanent more than once")
    void cannotTargetSameTwice() {
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.addToBattlefield(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<Permanent> enemy = gd.playerBattlefields.get(player2.getId());
        UUID first = enemy.get(0).getId();
        UUID second = enemy.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first, second, first)))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AshenmoorGouger;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheOversoul;
import com.github.laxika.magicalvibes.cards.t.TurnToMist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildSwing.class, AshenmoorGouger.class, GreaterAuramancy.class, WoodedBastion.class,
        ShieldOfTheOversoul.class, TurnToMist.class})
class WildSwingTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Destroys exactly one of the three targets at random")
    void destroysOneOfThree() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<UUID> targets = List.of(first.getId(), second.getId(), third.getId());

        harness.castAndResolveSorcery(player1, 0, targets);

        // One at random dies, the other two remain.
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent auramancy = harness.addToBattlefieldAndReturn(player1, new GreaterAuramancy());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        List<UUID> targets = List.of(auramancy.getId(), first.getId(), second.getId());

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
        UUID first = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()).getId();
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first, second, first)))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4})
    void requiresExactlyThreeTargets(int targetCount) {
        List<UUID> targets = java.util.stream.IntStream.range(0, targetCount)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()).getId())
                .toList();
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void choosesOnlyTargetsThatRemainOnTheBattlefield(int exiledCount) {
        List<UUID> targets = java.util.stream.IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()).getId())
                .toList();
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();
        harness.castSorcery(player1, 0, targets);

        for (int i = 0; i < exiledCount; i++) {
            harness.setHand(player1, List.of(new TurnToMist()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castAndResolveInstant(player1, 0, targets.get(i));
        }
        harness.passBothPriorities();

        int destroyedCount = exiledCount == 3 ? 0 : 1;
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3 - exiledCount - destroyedCount);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(destroyedCount);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(exiledCount);
        harness.assertInGraveyard(player1, "Wild Swing");
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void excludesTargetsThatGainShroudBeforeResolution(int shroudedCount) {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger()))
                .toList();
        for (int i = 0; i < shroudedCount; i++) {
            Permanent aura = harness.addToBattlefieldAndReturn(player2, new ShieldOfTheOversoul());
            aura.setAttachedTo(creatures.get(i).getId());
        }
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();
        harness.castSorcery(player1, 0, creatures.stream().map(Permanent::getId).toList());

        harness.addToBattlefield(player2, new GreaterAuramancy());
        harness.passBothPriorities();

        int destroyedCount = shroudedCount == 3 ? 0 : 1;
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(4 + shroudedCount - destroyedCount)
                .containsAll(creatures.subList(0, shroudedCount));
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(destroyedCount);
        harness.assertInGraveyard(player1, "Wild Swing");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyAPermanentControlledByTheCaster() {
        List<UUID> targets = java.util.stream.IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new WoodedBastion()).getId())
                .toList();
        harness.setHand(player1, List.of(new WildSwing()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, targets);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Wooded Bastion");
        harness.assertInGraveyard(player1, "Wild Swing");
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindGames.class, AngelsFeather.class, Forest.class, GrizzlyBears.class, Pacifism.class})
class MindGamesTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target artifact, creature, or land")
    void tapsTargetArtifactCreatureOrLand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        for (Permanent target : List.of(artifact, creature, land)) {
            harness.setHand(player1, List.of(new MindGames()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.castAndResolveInstant(player1, 0, target.getId());
        }

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a permanent controlled by the caster")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment or player")
    void cannotTargetEnchantmentOrPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without buyback the spell taps its target and goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Mind Games");
    }

    @Test
    @DisplayName("Paying buyback returns the spell to its owner's hand")
    void buybackReturnsToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Mind Games");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Buyback still returns the spell when its target is already tapped")
    void buybackWithAlreadyTappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Mind Games");
        harness.assertNotInGraveyard(player1, "Mind Games");
    }

    @Test
    @DisplayName("Buyback does not return the spell when its only target leaves the battlefield")
    void illegalTargetPreventsBuybackReturn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Games");
        harness.assertNotInHand(player1, "Mind Games");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Buyback requires a second blue mana in addition to two generic mana")
    void buybackCannotBePaidWithOnlyOneBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindGames()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Mind Games");
        assertThat(target.isTapped()).isFalse();
    }
}

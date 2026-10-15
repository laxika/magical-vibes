package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcceptableLosses.class, AvenFlock.class, AshenFirebeast.class, Forest.class})
class AcceptableLossesTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card and deals 5 damage to target creature")
    void discardsAndDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertInGraveyard(player1, "Acceptable Losses");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a creature that survives")
    void dealsExactlyFiveDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshenFirebeast());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot cast without another card in hand for the random discard")
    void cannotCastWithoutAnotherCardInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new AcceptableLosses()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays exactly one random discard before resolution, excluding the spell itself")
    void paysDiscardBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        AcceptableLosses spell = new AcceptableLosses();
        Forest forest = new Forest();
        AvenFlock flock = new AvenFlock();
        harness.setHand(player1, List.of(forest, spell, flock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 1, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> card == forest || card == flock);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1)
                .allMatch(card -> card == forest || card == flock);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Aven Flock");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Acceptable Losses");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Aven Flock");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenFlock());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Aven Flock");
        harness.assertInGraveyard(player1, "Acceptable Losses");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}

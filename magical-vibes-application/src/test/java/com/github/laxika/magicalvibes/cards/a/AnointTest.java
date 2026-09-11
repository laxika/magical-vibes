package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Anoint.class, TrainedArmodon.class, LightningBlast.class, MetallicSliver.class})
class AnointTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Anoint targets a creature and goes on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Trained Armodon");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(entry.isBuyback()).isFalse();
    }

    @Test
    @DisplayName("Resolving Anoint without buyback prevents the next 3 damage to the target")
    void resolvingAddsShield() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(armodon(player1).getDamagePreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard as it resolves")
    void resolvesToGraveyardWithoutBuyback() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(playerHandNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        harness.castInstantWithBuyback(player1, 0, targetId);

        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Anoint");
        assertThat(playerHandNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Buyback spells that fizzle still go to the graveyard")
    void buybackFizzleGoesToGraveyard() {
        // Target leaves the battlefield before Anoint resolves — the spell fizzles and, because
        // it never resolved, buyback does not return it to hand.
        harness.addToBattlefield(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Trained Armodon");
        harness.castInstantWithBuyback(player1, 0, targetId);

        // Opponent destroys the target while Anoint is on the stack.
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(playerHandNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Anoint");
    }

    @Test
    @DisplayName("Paying buyback with insufficient mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        // The failed cast left the card in hand and the mana pool untouched.
        assertThat(playerHandNames(player1)).containsExactly("Anoint");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("The prevention shield is consumed by incoming damage")
    void shieldPreventsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = target.getId();
        harness.castAndResolveInstant(player1, 0, targetId);

        // Lightning Blast deals 4 damage: 3 is prevented and 1 gets through.
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Anoint only prevents damage to its chosen creature")
    void shieldOnlyAppliesToTargetCreature() {
        Permanent shielded = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        Permanent unshielded = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, shielded.getId());

        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, unshielded.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(shielded.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(unshielded.getId()));
        assertThat(shielded.getDamagePreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void shieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Trained Armodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(armodon(player1).getDamagePreventionShield()).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(armodon(player1).getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Anoint cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent armodon(Player player) {
        return findPermanent(player, "Trained Armodon");
    }

    private List<String> playerHandNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}

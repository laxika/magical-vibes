package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralHelm.class, Forest.class, GrizzlyBears.class})
class CoralHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Ability pumps target creature +2/+2 and discards a card at random as a cost")
    void pumpsTargetAndDiscardsAtRandom() {
        harness.addToBattlefield(player1, new CoralHelm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        int helmIndex = battlefieldIndex(player1, "Coral Helm");
        harness.activateAbility(player1, helmIndex, null, bearId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }
    @Test
    @DisplayName("Pays one random discard immediately when the ability is activated")
    void paysRandomDiscardAsActivationCost() {
        harness.addToBattlefield(player1, new CoralHelm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bearId);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        harness.passBothPriorities();
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new CoralHelm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate with an empty hand (no card to discard)")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new CoralHelm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CoralHelm());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID helmId = harness.getPermanentId(player1, "Coral Helm");
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, helmId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent creature")
    void pumpsOpponentsCreature() {
        harness.addToBattlefield(player1, new CoralHelm());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }
    @Test
    @DisplayName("A tapped Helm can activate repeatedly and its boosts add together")
    void tappedHelmCanActivateRepeatedly() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new CoralHelm());
        helm.tap();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bear.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(player1, "Coral Helm"), null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(helm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with less than three mana and does not discard")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new CoralHelm());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Coral Helm"), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, cardName));
    }
}

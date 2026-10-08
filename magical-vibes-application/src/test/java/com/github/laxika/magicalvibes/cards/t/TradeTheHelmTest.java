package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuardianBeast;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TradeTheHelm.class, GrizzlyBears.class, HowlingMine.class, Pacifism.class,
        GuardianBeast.class, Unsummon.class})
class TradeTheHelmTest extends BaseCardTest {

    private void prepareSpell() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of an artifact you control and a creature an opponent controls")
    void exchangesControlOfArtifactAndCreature() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(ownArtifact.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("Rejects a second target that is not an artifact or creature an opponent controls")
    void rejectsIllegalOpponentTarget() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        opponentEnchantment.setAttachedTo(enchantedCreature.getId());

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(ownArtifact.getId(), opponentEnchantment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Second target must be an artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("Cycling discards Trade the Helm and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Trade the Helm");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void exchangesOwnCreatureForOpponentArtifact() {
        prepareSpell();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        ownCreature.tap();
        opponentArtifact.tap();

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentArtifact).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature).doesNotContain(opponentArtifact);
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentArtifact.isTapped()).isTrue();
    }

    @Test
    void exchangesTwoCreatures() {
        prepareSpell();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature).doesNotContain(opponentCreature);
    }

    @Test
    void exchangesTwoArtifacts() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        harness.castAndResolveSorcery(player1, 0, List.of(ownArtifact.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentArtifact).doesNotContain(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact).doesNotContain(opponentArtifact);
    }

    @Test
    void rejectsFirstTargetControlledByOpponent() {
        prepareSpell();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsSecondTargetControlledByCaster() {
        prepareSpell();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotExchangeWhenOpponentTargetLeavesBattlefield() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(ownArtifact.getId(), opponentCreature.getId()));
        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ownArtifact, opponentCreature);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Trade the Helm");
    }

    @Test
    void doesNotExchangeWhenOwnTargetLeavesBattlefield() {
        prepareSpell();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opponentArtifact.getId()));
        harness.castAndResolveInstant(player2, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature, opponentArtifact);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Trade the Helm");
    }

    @Test
    void doesNotExchangeWhenOwnArtifactCannotChangeControl() {
        prepareSpell();
        harness.addToBattlefield(player1, new GuardianBeast());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(ownArtifact.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature).doesNotContain(ownArtifact);
    }

    @Test
    void doesNotExchangeWhenOpponentArtifactCannotChangeControl() {
        prepareSpell();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GuardianBeast());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature).doesNotContain(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact).doesNotContain(ownCreature);
    }

    @Test
    void exchangesGuardianBeastAndOpponentArtifactSimultaneously() {
        prepareSpell();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GuardianBeast());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentArtifact).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature).doesNotContain(opponentArtifact);
    }

    @Test
    void cyclingPaysDiscardCostBeforeDrawing() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Trade the Helm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCycleWithOnlyOneMana() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Trade the Helm");
        harness.assertNotInGraveyard(player1, "Trade the Helm");
        assertThat(gd.stack).isEmpty();
    }
}

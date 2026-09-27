package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({VeilOfSecrecy.class, FirstVolley.class, TeardropKami.class, GnarledMass.class, MendingHands.class})
class VeilOfSecrecyTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains shroud and can't be blocked")
    void grantsShroudAndUnblockable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Shroud and unblockable wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = player1.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell by returning a blue creature, staying in hand")
    void splicesOntoArcaneSpell() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(blueCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Teardrop Kami"));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Veil of Secrecy", "Teardrop Kami");
    }

    @Test
    @DisplayName("Splicing adds Veil of Secrecy's effects to the Arcane host spell")
    void splicedEffectsAreAddedToHostSpell() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(blueCreature.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane instant")
    void cannotSpliceOntoNonArcaneInstant() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MendingHands(), new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(blueCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the splice cost by returning a blue creature an opponent controls")
    void cannotReturnOpponentBlueCreatureForSplice() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TeardropKami());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1),
                List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the splice cost by returning a creature that is not blue")
    void cannotReturnNonBlueCreatureForSplice() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new FirstVolley(), new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1), List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}

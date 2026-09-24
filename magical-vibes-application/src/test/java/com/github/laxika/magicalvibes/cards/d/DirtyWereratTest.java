package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Halberdier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirtyWererat.class, Forest.class, Halberdier.class})
class DirtyWereratTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants a regeneration shield")
    void discardCardRegenerates() {
        Permanent wererat = harness.addToBattlefieldAndReturn(player1, new DirtyWererat());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(wererat.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Threshold grants +2/+2 and prevents blocking")
    void thresholdBoostsAndPreventsBlocking() {
        Permanent attacker = addAttacker();
        Permanent wererat = addCreatureReady(player2, new DirtyWererat());
        int basePower = gqs.getEffectivePower(gd, wererat);
        int baseToughness = gqs.getEffectiveToughness(gd, wererat);

        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness + 2);

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(wererat),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Threshold effects disappear below seven graveyard cards")
    void thresholdEffectsDisappearBelowSevenCards() {
        Permanent wererat = addCreatureReady(player2, new DirtyWererat());
        int basePower = gqs.getEffectivePower(gd, wererat);
        int baseToughness = gqs.getEffectiveToughness(gd, wererat);
        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness + 2);

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        Permanent wererat = addCreatureReady(player2, new DirtyWererat());
        int basePower = gqs.getEffectivePower(gd, wererat);
        int baseToughness = gqs.getEffectiveToughness(gd, wererat);

        harness.setGraveyard(player1, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Below threshold the creature can block")
    void belowThresholdCreatureCanBlock() {
        Permanent wererat = addCreatureReady(player2, new DirtyWererat());
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, graveyardWithCards(6));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(wererat),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(wererat.isBlocking()).isTrue();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Halberdier());
        attacker.setAttacking(true);
        return attacker;
    }

    private List<Card> graveyardWithSevenCards() {
        return graveyardWithCards(7);
    }

    private List<Card> graveyardWithCards(int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}

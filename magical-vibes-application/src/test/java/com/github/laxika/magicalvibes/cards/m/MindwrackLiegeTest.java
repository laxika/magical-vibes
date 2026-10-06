package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NoggleBandit;
import com.github.laxika.magicalvibes.cards.o.OonasGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindwrackLiege.class, FugitiveWizard.class, HillGiant.class, GrizzlyBears.class,
        AirElemental.class, NoggleBandit.class, OonasGrace.class})
class MindwrackLiegeTest extends BaseCardTest {

    // ===== Static effects: +1/+1 to own blue / red creatures =====

    @Test
    @DisplayName("Other blue creatures you control get +1/+1")
    void buffsOwnBlueCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOwnRedCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff non-blue non-red creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Activated ability: put a blue or red creature card onto the battlefield =====

    @Test
    @DisplayName("Only blue or red creature cards in hand are valid choices")
    void onlyBlueOrRedCreaturesAreValidChoices() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        // Blue (0), off-color green (1), red (2).
        harness.setHand(player1, List.of(new FugitiveWizard(), new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("Choosing a red creature puts it onto the battlefield")
    void choosingRedCreaturePutsItOntoBattlefield() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the may leaves hand and battlefield unchanged")
    void decliningMayLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addMana(player1, ManaColor.BLUE, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The Liege does not receive its own bonuses")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new MindwrackLiege());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blue and red creatures receive both bonuses, which stack across Lieges")
    void bothColorBonusesStackAcrossLieges() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MindwrackLiege());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MindwrackLiege());
        Permanent bandit = harness.addToBattlefieldAndReturn(player1, new NoggleBandit());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opposing blue and red creatures receive neither bonus")
    void doesNotBuffOpposingCreatures() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        Permanent bandit = harness.addToBattlefieldAndReturn(player2, new NoggleBandit());

        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mixed hybrid mana puts a blue creature onto the battlefield without paying its cost")
    void mixedHybridManaPutsBlueCreatureOntoBattlefield() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new MindwrackLiege());
        liege.tap();
        liege.setSummoningSick(true);
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.isTapped()).isFalse();
        assertThat(elemental.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(5);
        harness.assertNotInHand(player1, "Air Elemental");
        assertThat(liege.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A blue and red creature is eligible, but a blue instant is not")
    void rejectsBlueNoncreatureAndAllowsBothColorCreature() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new OonasGrace(), new NoggleBandit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);

        Permanent bandit = findPermanent(player1, "Noggle Bandit");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(4);
        harness.assertInHand(player1, "Oona's Grace");
        harness.assertNotInHand(player1, "Noggle Bandit");
    }

    @Test
    @DisplayName("Accepting with no eligible creature finishes without moving a card")
    void noEligibleCreatureLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Oona's Grace");
        harness.assertNotOnBattlefield(player1, "Oona's Grace");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four mana of an unrelated color cannot pay the hybrid activation cost")
    void cannotPayHybridCostWithGreenMana() {
        harness.addToBattlefield(player1, new MindwrackLiege());
        harness.addMana(player1, ManaColor.GREEN, 4);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}

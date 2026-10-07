package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.h.HedronArchive;
import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.cards.u.UlamogTheCeaselessHunger;
import com.github.laxika.magicalvibes.cards.v.VileAggregate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitansPresence.class, BroodhunterWurm.class, HedronArchive.class, KozileksChanneler.class,
        UlamogTheCeaselessHunger.class, VileAggregate.class})
class TitansPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature whose power is less than the revealed creature's power")
    void exilesCreatureWithinRevealedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        UlamogTheCeaselessHunger revealed = new UlamogTheCeaselessHunger();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Exiles a target creature whose power equals the revealed creature's power")
    void exilesCreatureAtRevealedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        KozileksChanneler revealed = new KozileksChanneler();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Leaves a target creature with greater power on the battlefield")
    void leavesCreatureWithGreaterPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UlamogTheCeaselessHunger());
        KozileksChanneler revealed = new KozileksChanneler();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("Requires revealing a colorless creature card")
    void requiresColorlessCreatureCardToReveal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence(), new BroodhunterWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be colorless creature card");
    }

    @Test
    void cannotCastWithoutRevealingACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must reveal colorless creature card");
    }

    @Test
    void cannotRevealAColorlessNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence(), new HedronArchive()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be colorless creature card");
    }

    @Test
    void leavesTargetWhosePowerIncreasesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence(), new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void exilesTargetWhosePowerDecreasesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UlamogTheCeaselessHunger());
        harness.setHand(player1, List.of(new TitansPresence(), new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        target.setPowerModifier(-6);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void stillExilesAfterRevealedCardLeavesHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        KozileksChanneler revealed = new KozileksChanneler();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(revealed));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void usesCharacteristicDefinedPowerOfRevealedCardInHand() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new KozileksChanneler());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence(), new VileAggregate()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void usesRevealedCardsPowerAtResolutionRatherThanAtCasting() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new KozileksChanneler());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new TitansPresence(), new VileAggregate()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.addToBattlefield(player1, new KozileksChanneler());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }
}

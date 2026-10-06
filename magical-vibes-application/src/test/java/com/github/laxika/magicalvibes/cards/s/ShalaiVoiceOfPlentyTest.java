package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ShalaiVoiceOfPlenty.class, AjaniGoldmane.class, GrizzlyBears.class, Shock.class})
class ShalaiVoiceOfPlentyTest extends BaseCardTest {

    // ===== Controller hexproof =====

    @Test
    @DisplayName("Controller has hexproof while Shalai is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent does not have hexproof from Shalai")
    void opponentDoesNotHaveHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());

        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Controller loses hexproof when Shalai leaves the battlefield")
    void controllerLosesHexproofWhenShalaiRemoved() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        Permanent shalai = findPermanent(player1, "Shalai, Voice of Plenty");
        gd.playerBattlefields.get(player1.getId()).remove(shalai);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    // ===== Other creatures hexproof =====

    @Test
    @DisplayName("Other creatures you control have hexproof")
    void otherCreaturesHaveHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Shalai itself does not have hexproof")
    void shalaiDoesNotHaveHexproof() {
        Permanent shalai = harness.addToBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());

        assertThat(gqs.hasKeyword(gd, shalai, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures do not get hexproof")
    void opponentCreaturesDoNotGetHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target hexproof creature with a spell")
    void opponentCannotTargetHexproofCreature() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Planeswalker hexproof =====

    @Test
    @DisplayName("Planeswalkers you control have hexproof")
    void planeswalkerHasHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());

        assertThat(gqs.hasKeyword(gd, ajani, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent's planeswalkers do not get hexproof")
    void opponentPlaneswalkerDoesNotGetHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        Permanent opponentAjani = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());

        assertThat(gqs.hasKeyword(gd, opponentAjani, Keyword.HEXPROOF)).isFalse();
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activated ability puts +1/+1 counter on each creature you control")
    void activatedAbilityPutsCounters() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent shalai = findPermanent(player1, "Shalai, Voice of Plenty");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shalai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does not affect opponent's creatures")
    void activatedAbilityDoesNotAffectOpponent() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    void opponentCannotTargetProtectedPlayer() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllerCanTargetOwnHexproofCreature() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void opponentCanTargetShalaiAndProtectionEndsWhenItDies() {
        Permanent shalai = harness.addToBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, shalai.getId());
        harness.castAndResolveInstant(player2, 0, shalai.getId());

        harness.assertInGraveyard(player1, "Shalai, Voice of Plenty");
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ajani, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void counterAbilityUsesCreaturesPresentAtResolutionAndSurvivesSourceLeaving() {
        Permanent shalai = harness.addToBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(shalai);
        gd.playerGraveyards.get(player1.getId()).add(shalai.getCard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ajani.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shalai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterAbilityCanBeActivatedRepeatedlyWithoutTapping() {
        Permanent shalai = harness.addToBattlefieldAndReturn(player1, new ShalaiVoiceOfPlenty());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shalai.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(shalai.isTapped()).isFalse();
    }
}

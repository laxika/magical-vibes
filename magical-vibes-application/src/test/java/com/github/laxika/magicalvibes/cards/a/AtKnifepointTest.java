package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtKnifepoint.class, DauthiMercenary.class, GrizzlyBears.class, Shock.class})
class AtKnifepointTest extends BaseCardTest {

    @Test
    @DisplayName("Outlaws you control have first strike during your turn")
    void grantsFirstStrikeToOutlawsYouControlDuringYourTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        Permanent nonOutlaw = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingOutlaw = harness.addToBattlefieldAndReturn(player2, new DauthiMercenary());
        harness.addToBattlefield(player1, new AtKnifepoint());

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Outlaws lose the granted first strike during an opponent's turn")
    void removesFirstStrikeDuringOpponentsTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A crime creates one Mercenary token and the token can boost a creature")
    void createsMercenaryTokenWithBoostAbility() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        commitCrime();

        List<Permanent> tokens = findPermanents(player1, "Mercenary");
        assertThat(tokens).hasSize(1);

        Permanent mercenary = tokens.getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        commitCrime();
        commitCrime();

        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    @Test
    void targetingYourselfDoesNotUseTheCrimeTrigger() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Mercenary")).isZero();

        commitCrime();
        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    @Test
    void opponentsCrimeDoesNotTriggerYourEnchantment() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mercenary")).isZero();
    }

    @Test
    void eachCopyTriggersIndependentlyAndTokensGainFirstStrike() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        commitCrime();
        commitCrime();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(2)
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isTrue());
    }

    @Test
    void crimeTriggerResetsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        commitCrime();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        commitCrime();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(2)
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.FIRST_STRIKE)).isFalse());
    }

    @Test
    void mercenaryCannotActivateWhileSummoningSick() {
        Permanent mercenary = createMercenary();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateOutsideMainPhase() {
        Permanent mercenary = createMercenary();
        mercenary.setSummoningSick(false);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotTargetAnOpponentsCreature() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mercenary = createMercenary();
        mercenary.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateInResponseToASpell() {
        Permanent mercenary = createMercenary();
        mercenary.setSummoningSick(false);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("stack is empty");
        assertThat(mercenary.isTapped()).isFalse();
        resolveAllTriggers();
    }

    @Test
    void mercenaryCanBoostItselfAndBoostExpiresAtEndOfTurn() {
        Permanent mercenary = createMercenary();
        mercenary.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new Shock()));
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        harness.activateAbility(player1, index, 0, null, mercenary.getId());
        resolveAllTriggers();
        assertThat(mercenary.getPowerModifier()).isEqualTo(1);
        assertThat(mercenary.getToughnessModifier()).isZero();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(mercenary.getPowerModifier()).isZero();
        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    private Permanent createMercenary() {
        harness.addToBattlefield(player1, new AtKnifepoint());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        commitCrime();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Mercenary");
    }

    private void commitCrime() {
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
    }
}

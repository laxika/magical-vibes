package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoSVanguard.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class ZurgoSVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 3")
    void powerEqualsControlledCreatures() {
        Permanent vanguard = addCreatureReady(player1, new ZurgoSVanguard());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);

        harness.addToBattlefield(player1, new ZurgoSVanguard());
        harness.addToBattlefield(player1, new ZurgoSVanguard());
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts only creatures controlled by its controller")
    void powerIgnoresOpponentsCreatures() {
        Permanent vanguard = addCreatureReady(player1, new ZurgoSVanguard());

        harness.addToBattlefield(player2, new ZurgoSVanguard());
        harness.addToBattlefield(player2, new ZurgoSVanguard());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Warrior token")
    void attackingCreatesMobilizedToken() {
        addCreatureReady(player1, new ZurgoSVanguard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        Permanent token = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(findPermanents(player1, "Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new ZurgoSVanguard());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Mobilized creatures increase power until they are sacrificed")
    void powerTracksMobilizedCreatures() {
        Permanent vanguard = addCreatureReady(player1, new ZurgoSVanguard());
        harness.addToBattlefield(player1, new ZurgoSVanguard());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mobilize resolves and sacrifices its token even after the source leaves")
    void mobilizeSurvivesSourceLeavingBattlefield() {
        Permanent vanguard = addCreatureReady(player1, new ZurgoSVanguard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        gd.playerBattlefields.get(player1.getId()).remove(vanguard);
        gd.playerGraveyards.get(player1.getId()).add(vanguard.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }

    @Test
    @DisplayName("The characteristic-defining power ability works in the graveyard")
    void powerIsDefinedOutsideBattlefield() {
        ZurgoSVanguard card = new ZurgoSVanguard();
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.addToBattlefield(player2, new ZurgoSVanguard());

        assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
        harness.addToBattlefield(player1, new ZurgoSVanguard());
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);
    }

    @Test
    @DisplayName("A mobilized token may attack a battle protected by the opponent")
    void mobilizedTokenMayAttackBattle() {
        addCreatureReady(player1, new ZurgoSVanguard());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(battle.getId());
        harness.handlePermanentChosen(player1, battle.getId());

        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(battle.getId());
    }
}

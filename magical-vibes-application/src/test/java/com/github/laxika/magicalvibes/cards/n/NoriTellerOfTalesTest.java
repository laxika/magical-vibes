package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LongBodiedGreyDog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoriTellerOfTales.class, LongBodiedGreyDog.class})
class NoriTellerOfTalesTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Nori attacks, target attacking creature gains first strike")
    void grantsFirstStrikeToTargetAttackingCreature() {
        Permanent nori = addCreatureReady(player1, new NoriTellerOfTales());
        Permanent attacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(nori.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nori, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new NoriTellerOfTales());
        addCreatureReady(player1, new LongBodiedGreyDog());
        Permanent nonAttacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new NoriTellerOfTales());
        Permanent attacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Nori can grant first strike to itself when attacking alone")
    void canTargetItself() {
        Permanent nori = addCreatureReady(player1, new NoriTellerOfTales());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, nori.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, nori, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Nori does not trigger when only another creature attacks")
    void doesNotTriggerWhenNotAttacking() {
        Permanent nori = addCreatureReady(player1, new NoriTellerOfTales());
        Permanent attacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, nori, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A target removed from combat before resolution does not gain first strike")
    void targetMustStillBeAttackingAtResolution() {
        addCreatureReady(player1, new NoriTellerOfTales());
        Permanent attacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves even if Nori leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent nori = addCreatureReady(player1, new NoriTellerOfTales());
        Permanent attacker = addCreatureReady(player1, new LongBodiedGreyDog());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(nori);
        gd.playerGraveyards.get(player1.getId()).add(nori.getCard());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }
}

package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NajeelaTheBladeBlossom.class, ElvishWarrior.class, GrizzlyBears.class})
class NajeelaTheBladeBlossomTestMarRegression extends BaseCardTest {

    @Test
    void warriorAttackOffersTokenToNajeelaControllerAndGivesItToWarriorController() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player2, "Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WARRIOR);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
        harness.assertNotOnBattlefield(player1, "Warrior");
    }

    @Test
    void combatAbilityUntapsAttackersGrantsKeywordsAndQueuesCombat() {
        Permanent najeela = addCreatureReady(player1, new NajeelaTheBladeBlossom());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.tap();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HASTE)).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(najeela.isAttacking()).isFalse();
    }

    @Test
    void combatAbilityCannotBeActivatedOutsideCombat() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}

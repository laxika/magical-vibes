package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NajeelaTheBladeBlossom.class, ElvishWarrior.class, GrizzlyBears.class})
class NajeelaTheBladeBlossomTest extends BaseCardTest {

    @Test
    @DisplayName("A Warrior attack may create a tapped and attacking token for that Warrior's controller")
    void warriorAttackCreatesTokenForAttackerController() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        addCreatureReady(player1, new ElvishWarrior());

        declareAttackersFor(player1, List.of(1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A Warrior controlled by an opponent creates its token under that opponent")
    void opponentWarriorCreatesTokenForOpponent() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        addCreatureReady(player2, new ElvishWarrior());

        declareAttackersFor(player2, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
        List<Permanent> tokens = findPermanents(player2, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Najeela's combat ability untaps attackers, grants keywords, and adds combat")
    void combatAbilityAddsCombatAndImprovesAttackers() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new GrizzlyBears()).tap();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        declareAttackersFor(player1, List.of(1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HASTE)).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private void declareAttackersFor(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}

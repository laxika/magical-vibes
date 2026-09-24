package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UsherOfTheFallen;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NajeelaTheBladeBlossom.class, ElvishWarrior.class, GrizzlyBears.class, UsherOfTheFallen.class})
class NajeelaTheBladeBlossomTest extends BaseCardTest {

    @Test
    @DisplayName("A Warrior attack may create a tapped and attacking white Warrior for its controller")
    void warriorAttackCreatesTokenForAttackerController() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        Permanent attacker = addCreatureReady(player1, new UsherOfTheFallen());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        Permanent token = findToken(player1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Najeela's controller chooses whether an opponent's Warrior creates a token")
    void opponentsWarriorCreatesTokenForOpponent() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        addCreatureReady(player2, new UsherOfTheFallen());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        Permanent token = findToken(player2);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Non-Warrior attacks do not trigger Najeela")
    void nonWarriorAttackDoesNotTrigger() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("The combat ability untaps attackers, grants keywords, and adds a combat")
    void combatAbilityUntapsAndGrantsAttackingCreatures() {
        addCreatureReady(player1, new NajeelaTheBladeBlossom());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        addFiveColorMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    private Permanent findToken(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void addFiveColorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}

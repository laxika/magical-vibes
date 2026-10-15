package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessFootAssault.class, GrizzlyBears.class})
class EndlessFootAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        castEndlessFootAssault(List.of("{1}{W}"));
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Endless Foot Assault");
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Casting without squad payments creates no copies")
    void noSquadPaymentCreatesNoCopies() {
        castEndlessFootAssault(List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Endless Foot Assault")).hasSize(1);
    }

    @Test
    @DisplayName("Multiple squad payments create exactly that many copies without recursion")
    void multipleSquadPaymentsCreateCopies() {
        castEndlessFootAssault(List.of("{1}{W}", "{1}{W}", "{1}{W}"));
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Endless Foot Assault");
        assertThat(copies).hasSize(4);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
    }

    @Test
    @DisplayName("Each squad copy triggers once even when multiple creatures attack")
    void squadCopiesEachTriggerOncePerAttack() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        castEndlessFootAssault(List.of("{1}{W}", "{1}{W}"));
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(first),
                    gd.playerBattlefields.get(player1.getId()).indexOf(second)));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Ninja")).hasSize(3).allSatisfy(ninja -> {
            assertThat(ninja.isTapped()).isTrue();
            assertThat(ninja.isAttacking()).isTrue();
            assertThat(ninja.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Declaring no attackers does not create Ninjas")
    void noAttackersCreatesNoNinjas() {
        castEndlessFootAssault(List.of());
        resolveAllTriggers();

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("An opponent attacking does not trigger the enchantment")
    void opponentAttackDoesNotCreateNinjas() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castEndlessFootAssault(List.of());
        resolveAllTriggers();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
        assertThat(findPermanents(player2, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("Attacking creates a tapped Ninja attacking each opponent")
    void attackCreatesNinjaAttackingOpponent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castEndlessFootAssault(List.of());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
            resolveAllTriggers();
        });

        List<Permanent> ninjas = findPermanents(player1, "Ninja");
        assertThat(ninjas).hasSize(1);
        assertThat(ninjas.getFirst().isTapped()).isTrue();
        assertThat(ninjas.getFirst().isAttacking()).isTrue();
        assertThat(ninjas.getFirst().isAttackedThisTurn()).isFalse();
        assertThat(ninjas.getFirst().getAttackTarget()).isEqualTo(player2.getId());
    }

    private void castEndlessFootAssault(List<String> repeatedAdditionalCosts) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EndlessFootAssault()));
        harness.addMana(player1, ManaColor.COLORLESS, 2 + repeatedAdditionalCosts.size());
        harness.addMana(player1, ManaColor.WHITE, 1 + repeatedAdditionalCosts.size());

        harness.castEnchantmentWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
        harness.passBothPriorities();
    }
}

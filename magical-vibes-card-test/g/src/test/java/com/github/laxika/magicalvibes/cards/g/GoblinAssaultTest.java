package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinAssault.class, GoblinDeathraiders.class, CylianElf.class})
class GoblinAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 red Goblin token with haste during controller's upkeep")
    void createsHastyGoblinDuringUpkeep() {
        harness.addToBattlefield(player1, new GoblinAssault());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent goblin = tokens.getFirst();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token during an opponent's upkeep (\"your upkeep\" only)")
    void noTokenDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new GoblinAssault());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve any triggers

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("A Goblin the controller controls must attack while Goblin Assault is out")
    void controllersGoblinMustAttack() {
        harness.addToBattlefield(player1, new GoblinAssault());

        addCreatureReady(player1, new GoblinDeathraiders());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A non-Goblin creature is not forced to attack by Goblin Assault")
    void nonGoblinNotForced() {
        harness.addToBattlefield(player1, new GoblinAssault());

        Permanent bears = addCreatureReady(player1, new CylianElf());

        // Cylian Elf is not a Goblin, so Goblin Assault imposes no must-attack requirement.
        declareAttackers(player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Goblin Assault forces an opponent's Goblin to attack on the opponent's turn")
    void opponentsGoblinMustAttack() {
        harness.addToBattlefield(player1, new GoblinAssault());

        addCreatureReady(player2, new GoblinDeathraiders());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Without Goblin Assault, a Goblin is free to stay back")
    void goblinNotForcedWithoutAssault() {
        Permanent piker = addCreatureReady(player1, new GoblinDeathraiders());

        declareAttackers(player1, List.of());

        assertThat(piker.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Goblin without haste is not required to attack")
    void summoningSickGoblinMayStayBack() {
        harness.addToBattlefield(player1, new GoblinAssault());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinDeathraiders());
        goblin.setSummoningSick(true);

        declareAttackers(player1, List.of());

        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A tapped Goblin is not required to attack")
    void tappedGoblinMayStayBack() {
        harness.addToBattlefield(player1, new GoblinAssault());
        Permanent goblin = addCreatureReady(player1, new GoblinDeathraiders());
        goblin.tap();

        declareAttackers(player1, List.of());

        assertThat(goblin.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The upkeep token must attack immediately because it has haste")
    void newTokenMustAttackAndCanAttack() {
        harness.addToBattlefield(player1, new GoblinAssault());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent goblin = findPermanent(player1, "Goblin");
        int goblinIndex = gd.playerBattlefields.get(player1.getId()).indexOf(goblin);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(goblinIndex)));

        assertThat(goblin.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An upkeep trigger still creates its token after Goblin Assault leaves")
    void upkeepTriggerSurvivesSourceLeaving() {
        Permanent assault = harness.addToBattlefieldAndReturn(player1, new GoblinAssault());
        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(assault);
        gd.playerGraveyards.get(player1.getId()).add(assault.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        declareAttackers(player1, List.of());
    }

    @Test
    @DisplayName("Each Goblin Assault creates its own upkeep token")
    void multipleAssaultsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new GoblinAssault());
        harness.addToBattlefield(player1, new GoblinAssault());
        advanceToUpkeep(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }
}

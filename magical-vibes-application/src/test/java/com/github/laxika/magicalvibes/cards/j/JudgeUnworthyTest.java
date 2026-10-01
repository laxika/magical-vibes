package com.github.laxika.magicalvibes.cards.j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.r.RiverOfTears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({JudgeUnworthy.class, BladeOfTheSixthPride.class, BlindPhantasm.class, RiverOfTears.class})
class JudgeUnworthyTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 3, then deals damage equal to the revealed card's mana value")
    void scriesThenDamagesAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        attacker.setAttacking(true);
        Card bottomCard1 = new RiverOfTears();
        Card bottomCard2 = new RiverOfTears();
        Card bottomCard3 = new RiverOfTears();
        Card topCard = new BladeOfTheSixthPride();
        harness.setLibrary(player1, List.of(bottomCard1, bottomCard2, bottomCard3, topCard));

        castJudgeUnworthy(attacker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(topCard, bottomCard1, bottomCard2, bottomCard3);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void canTargetBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        blocker.setBlocking(true);
        harness.setLibrary(player1, List.of(new RiverOfTears()));

        castJudgeUnworthy(blocker);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertOnBattlefield(player2, "Blind Phantasm");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An empty library reveals no card and deals no damage")
    void emptyLibraryDealsNoDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of());

        castJudgeUnworthy(attacker);

        harness.assertOnBattlefield(player2, "Blind Phantasm");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        prepareJudgeUnworthy();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castJudgeUnworthy(Permanent target) {
        prepareJudgeUnworthy();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareJudgeUnworthy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JudgeUnworthy()));
        addJudgeUnworthyMana();
    }

    private void addJudgeUnworthyMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreyKnightParagon.class, GrizzlyBears.class, GrinningDemon.class})
class GreyKnightParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys a non-Demon attacking creature")
    void destroysNonDemonAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grey Knight Paragon");
    }

    @Test
    @DisplayName("Enters and exiles an attacking Demon")
    void exilesDemonAttacker() {
        Permanent attacker = addAttacker(player2, new GrinningDemon());

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertNotOnBattlefield(player2, "Grinning Demon");
        harness.assertNotInGraveyard(player2, "Grinning Demon");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grinning Demon");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GreyKnightParagon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private void castGreyKnightParagon(Permanent target) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GreyKnightParagon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, target.getId());
    }

    private void resolveEntryTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrappyBruiser.class, GrizzlyBears.class})
class ScrappyBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts another attacking creature and gives it trample")
    void boostsAnotherAttackingCreature() {
        harness.setLife(player2, 20);
        addReadyCreature(player1, new ScrappyBruiser());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The targeted attacker returns to its owner's hand at end of combat")
    void returnsTargetedAttackerAtEndOfCombat() {
        harness.setLife(player2, 20);
        addReadyCreature(player1, new ScrappyBruiser());
        addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger can resolve without a target")
    void canChooseNoTarget() {
        harness.setLife(player2, 20);
        addReadyCreature(player1, new ScrappyBruiser());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        resolveCombat();

        harness.assertOnBattlefield(player1, "Scrappy Bruiser");
    }

    @Test
    @DisplayName("A non-attacking creature cannot be targeted")
    void cannotTargetNonAttackingCreature() {
        addReadyCreature(player1, new ScrappyBruiser());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

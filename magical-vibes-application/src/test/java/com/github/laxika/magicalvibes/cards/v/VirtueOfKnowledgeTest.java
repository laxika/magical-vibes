package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtueOfKnowledge.class, VantressVisions.class, AuthorityOfTheConsuls.class,
        ProdigalPyromancer.class, FugitiveWizard.class})
class VirtueOfKnowledgeTest extends BaseCardTest {

    @Test
    void doublesTriggeredAbilitiesCausedByAnyPlayersPermanentEntering() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void adventureCopiesControlledActivatedAbility() {
        harness.setLife(player2, 20);
        addReadyPyromancer(player1);
        VirtueOfKnowledge card = new VirtueOfKnowledge();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int pyromancerIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();

        harness.castAdventure(player1, 0, pyromancerAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void adventureCannotTargetOpponentControlledAbility() {
        harness.setHand(player1, List.of(new VirtueOfKnowledge()));
        addReadyPyromancer(player2);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyPyromancer(Player player) {
        var permanent = harness.addToBattlefieldAndReturn(player, new ProdigalPyromancer());
        permanent.setSummoningSick(false);
    }
}

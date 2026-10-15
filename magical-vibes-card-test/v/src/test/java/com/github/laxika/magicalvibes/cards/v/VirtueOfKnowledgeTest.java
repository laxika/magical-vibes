package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
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
        ProdigalPyromancer.class, FugitiveWizard.class, UpTheBeanstalk.class})
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

    @Test
    void twoVirtuesCauseThreeOwnEntersTriggers() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        harness.setHand(player1, List.of(new UpTheBeanstalk()));
        harness.setLibrary(player1, List.of(new VirtueOfKnowledge(),
                new VirtueOfKnowledge(), new VirtueOfKnowledge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotDoubleOpponentsOwnEntersTrigger() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new UpTheBeanstalk()));
        harness.setLibrary(player2, List.of(new VirtueOfKnowledge()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void adventureCopiesTriggeredAbilityAndCanThenBeCastFromExile() {
        VirtueOfKnowledge virtue = new VirtueOfKnowledge();
        harness.setHand(player1, List.of(new UpTheBeanstalk(), virtue));
        harness.setLibrary(player1, List.of(new UpTheBeanstalk(), new UpTheBeanstalk(),
                new UpTheBeanstalk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        UUID triggerId = gd.stack.getLast().getTargetableId();
        harness.castAdventure(player1, 0, triggerId);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, virtue.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Virtue of Knowledge");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void adventureMayRetargetCopyWithoutChangingOriginal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyPyromancer(player1);
        harness.setHand(player1, List.of(new VirtueOfKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.castAdventure(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void adventureCannotTargetControlledSpell() {
        harness.setHand(player1, List.of(new UpTheBeanstalk(), new VirtueOfKnowledge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        UUID spellId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, spellId))
                .isInstanceOf(IllegalStateException.class);
    }
}

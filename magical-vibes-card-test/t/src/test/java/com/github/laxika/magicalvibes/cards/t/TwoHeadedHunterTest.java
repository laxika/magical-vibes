package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwoHeadedHunter.class, TwiceTheRage.class, Island.class})
class TwoHeadedHunterTest extends BaseCardTest {

    @Test
    void adventureGivesTargetCreatureDoubleStrikeUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TwoHeadedHunter());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void adventureCannotTargetNonCreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TwoHeadedHunter());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwoHeadedHunter());
        harness.setHand(player1, List.of(new TwoHeadedHunter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwoHeadedHunter());
        TwoHeadedHunter card = new TwoHeadedHunter();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Two-Headed Hunter");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void menacePreventsSingleBlocker() {
        addCreatureReady(player1, new TwoHeadedHunter());
        addCreatureReady(player2, new TwoHeadedHunter());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new TwoHeadedHunter());
        Permanent first = addCreatureReady(player2, new TwoHeadedHunter());
        Permanent second = addCreatureReady(player2, new TwoHeadedHunter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}

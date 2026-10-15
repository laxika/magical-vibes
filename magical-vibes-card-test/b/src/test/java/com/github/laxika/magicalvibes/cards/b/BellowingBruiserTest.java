package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BellowingBruiser.class, BeatAPath.class, GrizzlyBears.class})
class BellowingBruiserTest extends BaseCardTest {

    @Test
    void adventureCanTargetOneCreatureYouControlWithoutAffectingOthers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BellowingBruiser());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BellowingBruiser());
        BellowingBruiser card = new BellowingBruiser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BellowingBruiser());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BellowingBruiser());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new BellowingBruiser());
        harness.setHand(player1, List.of(new BellowingBruiser()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BellowingBruiser());
        harness.setHand(player1, List.of(new BellowingBruiser()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureCanAttackTheTurnItIsCast() {
        harness.setHand(player1, List.of(new BellowingBruiser()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Bellowing Bruiser");
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    void adventureMakesUpToTwoCreaturesUnableToBlockThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BellowingBruiser card = new BellowingBruiser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCanBeCastWithNoTargets() {
        BellowingBruiser card = new BellowingBruiser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanTargetOnlyCreatures() {
        BellowingBruiser card = new BellowingBruiser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        BellowingBruiser card = new BellowingBruiser();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bellowing Bruiser");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }
}

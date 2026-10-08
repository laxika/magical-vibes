package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GazeInWonder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VelvetwingButterflies.class, GazeInWonder.class, Island.class})
class VelvetwingButterfliesTest extends BaseCardTest {

    @Test
    void adventureTapsOneTargetAndExilesTheCard() {
        Permanent target = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureTapsTwoTargetCreatures() {
        Permanent first = addCreatureReady(player2, new VelvetwingButterflies());
        Permanent second = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void adventureCreatureFaceCanBeCastFromExile() {
        Permanent target = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Velvetwing Butterflies");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCannotTargetMoreThanTwoCreatures() {
        Permanent first = addCreatureReady(player2, new VelvetwingButterflies());
        Permanent second = addCreatureReady(player2, new VelvetwingButterflies());
        Permanent third = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    void adventureCannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void creatureFaceCanBeCastDirectlyFromHand() {
        VelvetwingButterflies card = new VelvetwingButterflies();

        harness.castFromHand(player1, card, "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Velvetwing Butterflies");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCannotBeCastWithoutATarget() {
        prepareAdventure(new VelvetwingButterflies());

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCanTargetCreaturesControlledByDifferentPlayers() {
        Permanent ownCreature = addCreatureReady(player1, new VelvetwingButterflies());
        Permanent opposingCreature = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureResolvesWhenTargetIsAlreadyTapped() {
        Permanent target = addCreatureReady(player2, new VelvetwingButterflies());
        target.tap();
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Velvetwing Butterflies");
    }

    @Test
    void adventureResolvesForRemainingLegalTarget() {
        Permanent first = addCreatureReady(player2, new VelvetwingButterflies());
        Permanent second = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Velvetwing Butterflies");
    }

    @Test
    void adventureGoesToGraveyardWhenAllTargetsLeave() {
        Permanent first = addCreatureReady(player2, new VelvetwingButterflies());
        Permanent second = addCreatureReady(player2, new VelvetwingButterflies());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Velvetwing Butterflies");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureCannotChooseTheSameCreatureTwice() {
        Permanent target = addCreatureReady(player2, new VelvetwingButterflies());
        prepareAdventure(new VelvetwingButterflies());

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    private void prepareAdventure(VelvetwingButterflies card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

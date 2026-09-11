package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GazeInWonder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VelvetwingButterflies.class, GazeInWonder.class, GrizzlyBears.class, Island.class})
class VelvetwingButterfliesTest extends BaseCardTest {

    @Test
    void adventureTapsOneTargetAndExilesTheCard() {
        Permanent target = addReadyCreature(new GrizzlyBears());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureTapsTwoTargetCreatures() {
        Permanent first = addReadyCreature(new GrizzlyBears());
        Permanent second = addReadyCreature(new GrizzlyBears());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void adventureCreatureFaceCanBeCastFromExile() {
        Permanent target = addReadyCreature(new GrizzlyBears());
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
        Permanent first = addReadyCreature(new GrizzlyBears());
        Permanent second = addReadyCreature(new GrizzlyBears());
        Permanent third = addReadyCreature(new GrizzlyBears());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    void adventureCannotTargetNoncreature() {
        addReadyCreature(new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        VelvetwingButterflies card = new VelvetwingButterflies();
        prepareAdventure(card);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareAdventure(VelvetwingButterflies card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

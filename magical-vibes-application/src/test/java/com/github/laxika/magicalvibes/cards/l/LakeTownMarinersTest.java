package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoneFishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LakeTownMariners.class, GoneFishing.class, Forest.class, GrizzlyBears.class})
class LakeTownMarinersTest extends BaseCardTest {

    @Test
    void adventureExilesAndReturnsCreatureAndLandUnderTheirOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).anyMatch(permanent -> permanent.getOriginalCard() == creature.getOriginalCard());
        assertThat(battlefield).anyMatch(permanent -> permanent.getOriginalCard() == land.getOriginalCard());
        assertThat(battlefield).noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(battlefield).noneMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LakeTownMariners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, List.of(opponentCreature.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land you control");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}

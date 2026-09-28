package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScalelordReckoner.class, Shock.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class ScalelordReckonerTest extends BaseCardTest {

    @Test
    void destroysTargetedOpponentNonlandPermanent() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player1, new ScalelordReckoner());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castOpponentShock(reckoner);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
    }

    @Test
    void cannotTargetAnOpponentLand() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player1, new ScalelordReckoner());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castOpponentShock(reckoner);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerWhenAnOpponentTargetsANonDragon() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player1, new ScalelordReckoner());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(reckoner);
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}

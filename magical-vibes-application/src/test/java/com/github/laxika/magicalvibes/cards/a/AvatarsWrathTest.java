package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarsWrath.class, GrizzlyBears.class})
class AvatarsWrathTest extends BaseCardTest {

    @Test
    void airbendsAllOtherCreaturesAndExilesItself() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        AvatarsWrath wrath = new AvatarsWrath();
        harness.setHand(player1, List.of(wrath));
        addAvatarWrathMana();

        harness.castSorcery(player1, 0, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen);
        assertThat(gd.findExiledCard(other.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(wrath.getId())).isNotNull();
    }

    @Test
    void withoutATargetAirbendsEveryCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        AvatarsWrath wrath = new AvatarsWrath();
        harness.setHand(player1, List.of(wrath));
        addAvatarWrathMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void opponentsCannotCastAirbentCreaturesUntilYourNextTurn() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvatarsWrath()));
        addAvatarWrathMana();

        harness.castSorcery(player1, 0, chosen.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, other.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be cast");
    }

    private void addAvatarWrathMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}

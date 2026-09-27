package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerheadTyrant.class, GrizzlyBears.class, Opt.class, SakuraTribeScout.class})
class HammerheadTyrantTest extends BaseCardTest {

    @Test
    void returnsAnOpponentPermanentWithManaValueAtMostTheCastSpell() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        Permanent eligible = addCreatureReady(player2, new SakuraTribeScout());
        Permanent tooExpensive = addCreatureReady(player2, new GrizzlyBears());
        castOpt();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(eligible.getId()).doesNotContain(tooExpensive.getId());

        harness.handlePermanentChosen(player1, eligible.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Sakura-Tribe Scout");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void mayChooseNoTargetWhenNoPermanentQualifies() {
        harness.addToBattlefield(player1, new HammerheadTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOpt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castOpt() {
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}

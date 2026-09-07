package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkReaper.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class})
class SparkReaperTest extends BaseCardTest {

    @Test
    void sacrificesCreatureGainsLifeAndDrawsCard() {
        Permanent reaper = addReady(new SparkReaper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(reaper), null, null);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canSacrificePlaneswalkerAndSourceIsAValidChoice() {
        Permanent reaper = addReady(new SparkReaper());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(reaper), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(reaper.getId(), planeswalker.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra Nalaar");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}

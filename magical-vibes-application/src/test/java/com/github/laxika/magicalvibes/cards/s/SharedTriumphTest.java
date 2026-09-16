package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharedTriumph.class, GoblinSkyRaider.class, ElvishWarrior.class})
class SharedTriumphTest extends BaseCardTest {

    @Test
    void creaturesOfChosenTypeOnEitherBattlefieldGetBoosted() {
        Permanent ownGoblin = addCreatureReady(player1, new GoblinSkyRaider());
        Permanent ownElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinSkyRaider());

        harness.castFromHand(player1, new SharedTriumph(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
    }

    @Test
    void boostStopsWhenSharedTriumphLeavesBattlefield() {
        Permanent goblin = addCreatureReady(player1, new GoblinSkyRaider());

        harness.castFromHand(player1, new SharedTriumph(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);

        Permanent sharedTriumph = findPermanent(player1, "Shared Triumph");
        gd.playerBattlefields.get(player1.getId()).remove(sharedTriumph);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }
}

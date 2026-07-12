package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepchannelMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature you control can't be blocked")
    void blueCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();
    }

    @Test
    @DisplayName("Non-blue creature you control can still be blocked")
    void nonBlueCreatureCanBeBlocked() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Does not affect opponent's blue creature")
    void doesNotAffectOpponentBlueCreatures() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent opponentWizard = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, opponentWizard)).isFalse();
    }

    @Test
    @DisplayName("Effect removed when Deepchannel Mentor leaves the battlefield")
    void effectRemovedWhenMentorLeaves() {
        harness.addToBattlefield(player1, new DeepchannelMentor());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Deepchannel Mentor"));

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isFalse();
    }
}

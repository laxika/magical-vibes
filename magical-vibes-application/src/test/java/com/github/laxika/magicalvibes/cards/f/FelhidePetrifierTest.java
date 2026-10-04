package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilenceTheBelievers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelhidePetrifier.class, FelhideMinotaur.class, GrizzlyBears.class, SilenceTheBelievers.class})
class FelhidePetrifierTest extends BaseCardTest {

    @Test
    @DisplayName("Grants deathtouch to other Minotaur creatures you control")
    void grantsDeathtouchToOtherMinotaurs() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not grant deathtouch to non-Minotaur creatures")
    void doesNotGrantDeathtouchToNonMinotaurs() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to an opponent's Minotaur")
    void doesNotGrantDeathtouchToOpponentsMinotaur() {
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player2, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Grants deathtouch to existing and newly entering Minotaurs")
    void grantsDeathtouchToExistingAndNewMinotaurs() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());
        assertThat(gqs.hasKeyword(gd, existing, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Granted deathtouch ends immediately when Petrifier leaves the battlefield")
    void losesGrantedDeathtouchWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isTrue();

        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, List.of(source.getId()));

        harness.assertNotOnBattlefield(player1, "Felhide Petrifier");
        harness.assertOnBattlefield(player1, "Felhide Minotaur");
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A remaining Petrifier continues to grant deathtouch")
    void remainingSourceKeepsDeathtouchActive() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FelhidePetrifier());
        harness.addToBattlefield(player1, new FelhidePetrifier());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new FelhideMinotaur());

        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        harness.assertOnBattlefield(player1, "Felhide Petrifier");
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DEATHTOUCH)).isTrue();
    }
}

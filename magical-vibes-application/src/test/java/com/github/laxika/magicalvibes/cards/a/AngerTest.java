package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Anger.class, Mountain.class, GrizzlyBears.class})
class AngerTest extends BaseCardTest {

    @Test
    @DisplayName("An Anger in the graveyard gives your creatures haste while you control a Mountain")
    void grantsHasteFromGraveyardWithMountain() {
        harness.setGraveyard(player1, List.of(new Anger()));
        harness.addToBattlefield(player1, new Mountain());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Anger's graveyard ability turns off without a Mountain or after Anger leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Anger anger = new Anger();
        harness.setGraveyard(player1, List.of(anger));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        Mountain mountain = new Mountain();
        harness.addToBattlefield(player1, mountain);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == mountain);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Anger requires its graveyard card's controller to control a Mountain")
    void mountainMustBeControlledByGraveyardCardController() {
        harness.setGraveyard(player1, List.of(new Anger()));
        harness.addToBattlefield(player2, new Mountain());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Anger does not grant its graveyard ability from the battlefield")
    void battlefieldAngerDoesNotGrantGraveyardAbility() {
        harness.addToBattlefield(player1, new Anger());
        harness.addToBattlefield(player1, new Mountain());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}

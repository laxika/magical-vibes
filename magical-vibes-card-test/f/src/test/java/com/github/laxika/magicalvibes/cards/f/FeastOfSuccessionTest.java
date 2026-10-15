package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarOfMight.class, FeastOfSuccession.class, GrizzlyBears.class})
class FeastOfSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("Gives every creature -4/-4 and makes its caster the monarch")
    void weakensAllCreaturesAndMakesCasterMonarch() {
        Permanent ownAvatar = addCreatureReady(player1, new AvatarOfMight());
        Permanent enemyAvatar = addCreatureReady(player2, new AvatarOfMight());

        castFeastOfSuccession();

        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enemyAvatar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enemyAvatar)).isEqualTo(4);
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creatures with toughness 4 or less die on both sides")
    void killsSmallCreaturesOnBothSides() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castFeastOfSuccession();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -4/-4 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyAvatar = addCreatureReady(player2, new AvatarOfMight());

        castFeastOfSuccession();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyAvatar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, enemyAvatar)).isEqualTo(8);
    }

    private void castFeastOfSuccession() {
        harness.castFromHand(player1, new FeastOfSuccession(), "{4}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Becomes the monarch even when there are no creatures")
    void becomesMonarchWithEmptyBattlefield() {
        castFeastOfSuccession();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Feast of Succession");
    }

    @Test
    @DisplayName("Takes the monarchy from the opponent")
    void takesMonarchyFromOpponent() {
        gd.monarchPlayerId = player2.getId();

        castFeastOfSuccession();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Still weakens creatures when the caster is already the monarch")
    void weakensCreaturesWhenAlreadyMonarch() {
        gd.monarchPlayerId = player1.getId();
        addCreatureReady(player2, new GrizzlyBears());

        castFeastOfSuccession();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotWeakenCreaturesEnteringLater() {
        castFeastOfSuccession();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}

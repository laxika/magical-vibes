package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyAvatar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, enemyAvatar)).isEqualTo(8);
    }

    private void castFeastOfSuccession() {
        harness.setHand(player1, List.of(new FeastOfSuccession()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}

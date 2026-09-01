package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchsVengeance.class, AvatarOfMight.class, GrizzlyBears.class})
class WitchsVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of the chosen type get -3/-3 on every battlefield")
    void weakensCreaturesOfChosenType() {
        Permanent ownAvatar = addReadyCreature(player1, new AvatarOfMight());
        Permanent opposingAvatar = addReadyCreature(player2, new AvatarOfMight());
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingBear = addReadyCreature(player2, new GrizzlyBears());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");

        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void modifierWearsOffAtEndOfTurn() {
        Permanent avatar = addReadyCreature(player2, new AvatarOfMight());

        castWitchsVengeance(player1);
        harness.handleListChoice(player1, "AVATAR");

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(8);
    }

    private void castWitchsVengeance(Player caster) {
        harness.setHand(caster, List.of(new WitchsVengeance()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castSorcery(caster, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}

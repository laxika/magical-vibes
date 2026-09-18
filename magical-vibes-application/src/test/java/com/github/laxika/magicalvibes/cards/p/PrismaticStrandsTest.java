package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCharm;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismaticStrands.class, SavannahLions.class, GrizzlyBears.class, GoblinRaider.class, IzzetCharm.class})
class PrismaticStrandsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color on resolution prevents damage from that color")
    void choosingColorOnResolutionPreventsMatchingDamage() {
        castAndChooseColor("RED");

        Permanent redAttacker = addCreatureReady(player1, new GoblinRaider());
        Permanent greenAttacker = addCreatureReady(player1, new GrizzlyBears());
        redAttacker.setAttacking(true);
        greenAttacker.setAttacking(true);

        harness.setLife(player2, 20);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.preventDamageFromColors).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Flashback taps an untapped white creature and exiles Prismatic Strands")
    void flashbackTapsWhiteCreatureAndExilesSpell() {
        Permanent lions = addCreatureReady(player1, new SavannahLions());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(lions.getId()));

        assertThat(lions.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotInGraveyard(player1, "Prismatic Strands");
    }

    @Test
    @DisplayName("Flashback cannot tap a nonwhite creature")
    void flashbackRequiresWhiteCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback cannot tap a tapped white creature")
    void flashbackRequiresUntappedWhiteCreature() {
        Permanent lions = addCreatureReady(player1, new SavannahLions());
        lions.tap();
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(lions.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback cannot tap a white creature another player controls")
    void flashbackRequiresCreatureYouControl() {
        Permanent lions = addCreatureReady(player2, new SavannahLions());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(lions.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents damage from either color of a multicolored source")
    void preventsDamageFromEitherColorOfMulticoloredSource() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndChooseColor("BLUE");

        harness.setHand(player1, List.of(new IzzetCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAndChooseColor(String color) {
        harness.castFromHand(player1, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color);
    }
}

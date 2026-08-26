package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismaticStrands.class, SavannahLions.class, GrizzlyBears.class, GoblinRaider.class})
class PrismaticStrandsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color on resolution prevents damage from that color")
    void choosingColorOnResolutionPreventsMatchingDamage() {
        castAndChooseColor("RED");

        Permanent redAttacker = readyAttacker(player1, new GoblinRaider());
        Permanent greenAttacker = readyAttacker(player1, new GrizzlyBears());
        redAttacker.setAttacking(true);
        greenAttacker.setAttacking(true);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.preventDamageFromColors).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Flashback taps an untapped white creature and exiles Prismatic Strands")
    void flashbackTapsWhiteCreatureAndExilesSpell() {
        Permanent lions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        lions.setSummoningSick(false);
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndChooseColor(String color) {
        harness.setHand(player1, List.of(new PrismaticStrands()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color);
    }

    private Permanent readyAttacker(Player player, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, card);
        attacker.setSummoningSick(false);
        return attacker;
    }
}

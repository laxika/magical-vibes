package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.f.FledglingDragon;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({PrismaticStrands.class, SuntailHawk.class, AnuridBarkripper.class,
        FledglingDragon.class, LavaDart.class})
class PrismaticStrandsTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color on resolution prevents damage from that color")
    void choosingColorOnResolutionPreventsMatchingDamage() {
        castAndChooseColor("RED");

        addCreatureReady(player1, new FledglingDragon());
        addCreatureReady(player1, new AnuridBarkripper());

        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Flashback taps an untapped white creature and exiles Prismatic Strands")
    void flashbackTapsWhiteCreatureAndExilesSpell() {
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(hawk.getId()));

        assertThat(hawk.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotInGraveyard(player1, "Prismatic Strands");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Prismatic Strands"));
    }

    @Test
    @DisplayName("Flashback cannot tap a nonwhite creature")
    void flashbackRequiresWhiteCreature() {
        Permanent barkripper = addCreatureReady(player1, new AnuridBarkripper());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(barkripper.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chosen-color prevention stops matching noncombat damage to players and creatures")
    void chosenColorPreventsMatchingNoncombatDamage() {
        castAndChooseColor("RED");

        Permanent targetCreature = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(targetCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Flashback requires an untapped white creature controlled by the caster")
    void flashbackRequiresUntappedWhiteCreatureYouControl() {
        Permanent tappedHawk = addCreatureReady(player1, new SuntailHawk());
        tappedHawk.tap();
        Permanent opponentHawk = addCreatureReady(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(tappedHawk.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(opponentHawk.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tappedHawk.isTapped()).isTrue();
        assertThat(opponentHawk.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Prismatic Strands");
    }

    @Test
    @DisplayName("Flashback can tap a summoning-sick white creature")
    void flashbackCanTapSummoningSickWhiteCreature() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new PrismaticStrands()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(hawk.getId()));

        assertThat(hawk.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotInGraveyard(player1, "Prismatic Strands");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Prismatic Strands"));
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

}

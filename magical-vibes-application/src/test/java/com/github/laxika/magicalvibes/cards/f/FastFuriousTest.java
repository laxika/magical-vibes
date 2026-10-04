package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FastFurious.class, Forest.class, GrizzlyBears.class, Mountain.class, SuntailHawk.class})
class FastFuriousTest extends BaseCardTest {

    @Test
    @DisplayName("Fast discards a card, then draws two cards")
    void fastDiscardsThenDrawsTwo() {
        GrizzlyBears discarded = new GrizzlyBears();
        Forest firstDraw = new Forest();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, new ArrayList<>(List.of(new FastFurious(), discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Furious deals 3 damage to each creature without flying")
    void furiousDamagesOnlyCreaturesWithoutFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new FastFurious()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(3);
        assertThat(hawk.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Fast draws two cards even when there is no card to discard")
    void fastDrawsWithAnEmptyHand() {
        Forest firstDraw = new Forest();
        Mountain secondDraw = new Mountain();
        harness.setHand(player1, List.of(new FastFurious()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Fast can be cast during an opponent's end step")
    void fastCanBeCastAtInstantSpeed() {
        Forest firstDraw = new Forest();
        Mountain secondDraw = new Mountain();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FastFurious(), discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));
        }

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Furious cannot be cast during an opponent's end step")
    void furiousRequiresSorceryTiming() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FastFurious()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fast // Furious");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Furious damages ground creatures on both sides without damaging players or lands")
    void furiousAffectsBothPlayersGroundCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FastFurious()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}

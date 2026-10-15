package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({UmbralJuke.class, GrizzlyBears.class, ChandraNalaar.class})
class UmbralJukeTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 makes the targeted player sacrifice a creature or planeswalker")
    void sacrificesTargetPlayersCreatureOrPlaneswalker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds()).contains(bears.getId(), chandra.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()))
                .anyMatch(permanent -> permanent.getId().equals(chandra.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 creates a 2/1 white and black flying Inkling")
    void createsInklingToken() {
        harness.setHand(player1, List.of(new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().isToken()).isTrue();
        assertThat(inkling.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(inkling.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void targetedPlayerCanChoosePlaneswalkerInsteadOfCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chandra.getId()));

        harness.assertInGraveyard(player2, "Chandra Nalaar");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(chandra);
        harness.assertNotOnBattlefield(player1, "Inkling");
    }

    @Test
    void canTargetSelfAndSacrificeOwnInkling() {
        harness.setHand(player1, List.of(new UmbralJuke(), new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Inkling");

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inkling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeModeResolvesWithNoEligiblePermanents() {
        harness.setHand(player1, List.of(new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Umbral Juke");
        harness.assertNotOnBattlefield(player1, "Inkling");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void sacrificeModeCannotTargetPermanentDirectly() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UmbralJuke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Umbral Juke");
        assertThat(gd.stack).isEmpty();
    }
}

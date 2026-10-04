package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AshBarrens;
import com.github.laxika.magicalvibes.cards.s.SecludedSteppe;
import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.y.YokedPlowbeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfTheForgotten.class, YokedPlowbeast.class, JungleWeaver.class,
        GrizzlyBears.class, Censor.class, AshBarrens.class, SecludedSteppe.class})
class HeraldOfTheForgottenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Herald returns any number of target permanent cards with cycling")
    void castingReturnsSelectedCyclingPermanents() {
        Card first = new YokedPlowbeast();
        Card second = new JungleWeaver();
        Card nonPermanent = new Censor();
        Card nonCycling = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, nonPermanent, nonCycling));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yoked Plowbeast");
        harness.assertOnBattlefield(player1, "Jungle Weaver");
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No valid cycling permanent cards means no graveyard choice")
    void noValidCardsMeansNoChoice() {
        harness.setGraveyard(player1, List.of(new Censor(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Herald returns selected cycling lands, including typecycling, from its controller's graveyard")
    void returnsSelectedCyclingLandsFromOwnGraveyard() {
        Card selected = new AshBarrens();
        Card unselected = new SecludedSteppe();
        Card opposing = new AshBarrens();
        harness.setGraveyard(player1, List.of(selected, unselected));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(selected.getId(), unselected.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.assertInGraveyard(player1, "Ash Barrens");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ash Barrens");
        harness.assertInGraveyard(player1, "Secluded Steppe");
        harness.assertInGraveyard(player2, "Ash Barrens");
        harness.assertNotOnBattlefield(player2, "Ash Barrens");
    }

    @Test
    @DisplayName("Any number permits choosing zero even when eligible cards exist")
    void mayChooseZeroTargets() {
        harness.setGraveyard(player1, List.of(new AshBarrens()));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Herald of the Forgotten");
        harness.assertInGraveyard(player1, "Ash Barrens");
        harness.assertNotOnBattlefield(player1, "Ash Barrens");
    }

    @Test
    @DisplayName("Herald entering without being cast does not trigger")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new AshBarrens()));

        harness.enterBattlefieldAndReturn(player1, new HeraldOfTheForgotten());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Herald of the Forgotten");
        harness.assertInGraveyard(player1, "Ash Barrens");
    }

    @Test
    @DisplayName("A returned cycling land retains its enters-tapped replacement")
    void returnedCyclingLandEntersTapped() {
        Card land = new SecludedSteppe();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new HeraldOfTheForgotten()));
        addHeraldMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Secluded Steppe");
        harness.assertNotInGraveyard(player1, "Secluded Steppe");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getId()))
                .findFirst().orElseThrow().isTapped()).isTrue();
    }

    private void addHeraldMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}

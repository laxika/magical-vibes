package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.DragonfireBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WayspeakerBodyguard.class, CrawWurm.class, DragonfireBlade.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class WayspeakerBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a nonland permanent card with mana value 2 or less")
    void etbReturnsEligiblePermanentCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castBodyguard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot return a land or a permanent with mana value greater than 2")
    void etbRejectsIneligibleCards() {
        harness.setGraveyard(player1, List.of(new Forest(), new CrawWurm()));

        castBodyguard();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Craw Wurm");
    }

    @Test
    @DisplayName("Flurry taps an opponent's creature on the second spell and not the third")
    void flurryTapsOnlyOnSecondSpell() {
        addCreatureReady(player1, new WayspeakerBodyguard());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        opponentCreature.untap();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB returns a noncreature permanent but rejects an instant and an opponent's card")
    void etbReturnsArtifactFromOwnGraveyardOnly() {
        DragonfireBlade blade = new DragonfireBlade();
        LightningBolt bolt = new LightningBolt();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(blade, bolt));
        harness.setGraveyard(player2, List.of(opponentCard));

        castBodyguard();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(blade.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dragonfire Blade");
        harness.assertNotInGraveyard(player1, "Dragonfire Blade");
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flurry counts the Bodyguard spell cast before it entered")
    void flurryCountsItsOwnSpellAsFirstSpell() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        castBodyguard();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    private void castBodyguard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WayspeakerBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}

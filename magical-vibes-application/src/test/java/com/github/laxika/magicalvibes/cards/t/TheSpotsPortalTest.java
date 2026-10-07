package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DocOckSinisterScientist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSpotsPortal.class, DocOckSinisterScientist.class, GrizzlyBears.class, FountainOfYouth.class})
class TheSpotsPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target creature on the bottom of its owner's library and loses 2 life without a Villain")
    void tucksCreatureAndLosesLifeWithoutVillain() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        cast(target);

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .last()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not lose life when controlling a Villain")
    void doesNotLoseLifeWithVillain() {
        addCreatureReady(player1, new DocOckSinisterScientist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(target);

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "The Spot's Portal");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TheSpotsPortal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("An opponent's Villain does not prevent the caster's life loss")
    void opposingVillainDoesNotPreventLifeLoss() {
        Permanent target = addCreatureReady(player2, new DocOckSinisterScientist());

        cast(target);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Doc Ock, Sinister Scientist");
    }

    @Test
    @DisplayName("Tucking your only Villain causes life loss after it leaves")
    void tuckingOnlyVillainCausesLifeLoss() {
        Permanent target = addCreatureReady(player1, new DocOckSinisterScientist());

        cast(target);

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Doc Ock, Sinister Scientist");
        assertThat(gd.playerDecks.get(player1.getId())).last()
                .extracting(Card::getName).isEqualTo("Doc Ock, Sinister Scientist");
    }

    @Test
    @DisplayName("A Villain gained before resolution prevents life loss")
    void checksVillainControlAtResolution() {
        Permanent target = addCreatureReady(player2, new DocOckSinisterScientist());
        harness.setHand(player1, List.of(new TheSpotsPortal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        addCreatureReady(player1, new DocOckSinisterScientist());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Doc Ock, Sinister Scientist");
        harness.assertOnBattlefield(player1, "Doc Ock, Sinister Scientist");
    }

    @Test
    @DisplayName("An illegal sole target prevents the entire spell including life loss")
    void illegalTargetPreventsLifeLoss() {
        Permanent target = addCreatureReady(player2, new DocOckSinisterScientist());
        harness.setHand(player1, List.of(new TheSpotsPortal(), new TheSpotsPortal()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertLife(player1, 18);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Doc Ock, Sinister Scientist");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("The Spot's Portal", "The Spot's Portal");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TheSpotsPortal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}

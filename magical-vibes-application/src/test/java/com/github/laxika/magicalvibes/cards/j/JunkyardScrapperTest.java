package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkyardScrapper.class, FountainOfYouth.class, DarksteelCitadel.class,
        CommandersSphere.class, LlanowarElves.class})
class JunkyardScrapperTest extends BaseCardTest {

    @Test
    void seeksRandomEligibleArtifactAndGrantsPermissionToCastIt() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card sought = new FountainOfYouth();
        harness.setLibrary(player1, List.of(sought, new DarksteelCitadel(), new LlanowarElves()));
        harness.setHand(player1, List.of(new CommandersSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(sought);
        assertThat(gd.exilePlayPermissions).containsEntry(sought.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Darksteel Citadel", "Llanowar Elves");

        harness.castFromExile(player1, sought.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void doesNotExileArtifactsWithManaValueEqualToTheEnteringArtifactOrArtifactLands() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card tooExpensive = new CommandersSphere();
        Card artifactLand = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(tooExpensive, artifactLand));
        harness.setHand(player1, List.of(new CommandersSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive, artifactLand);
    }

    @Test
    void zeroManaArtifactCannotExileAnotherZeroManaArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate));
        harness.setHand(player1, List.of(new FountainOfYouth()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(candidate);
    }

    @Test
    void opponentsArtifactDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new CommandersSphere()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(candidate);
    }

    @Test
    void tokenArtifactAndNonartifactEntriesDoNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate));
        Card token = new CommandersSphere();
        token.setToken(true);

        harness.enterBattlefieldAndReturn(player1, token);
        harness.enterBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(candidate);
    }

    @Test
    void exilesExactlyOneEligibleCardWithoutReorderingTheRemainingLibrary() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card first = new FountainOfYouth();
        Card second = new FountainOfYouth();
        Card nonartifact = new LlanowarElves();
        Card land = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(nonartifact, first, land, second));

        harness.enterBattlefieldAndReturn(player1, new CommandersSphere());
        harness.passBothPriorities();

        List<Card> exiled = gd.getCardsExiledByPermanent(source.getId());
        assertThat(exiled).hasSize(1);
        assertThat(exiled.getFirst()).isIn(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                List.of(nonartifact, first, land, second).stream()
                        .filter(card -> card != exiled.getFirst()).toList());
    }

    @Test
    void triggerStillResolvesAndAllowsCastingAfterScrapperLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate));
        harness.enterBattlefieldAndReturn(player1, new CommandersSphere());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);

        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsEntry(candidate.getId(), player1.getId());
        harness.castFromExile(player1, candidate.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void permissionLastsThroughControllersNextTurnAndThenExpires() {
        harness.addToBattlefield(player1, new JunkyardScrapper());
        Card candidate = new FountainOfYouth();
        harness.setLibrary(player1, List.of(candidate, new LlanowarElves(), new LlanowarElves()));
        harness.setHand(player1, List.of(new CommandersSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(candidate.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(candidate.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(candidate.getId());
    }
}

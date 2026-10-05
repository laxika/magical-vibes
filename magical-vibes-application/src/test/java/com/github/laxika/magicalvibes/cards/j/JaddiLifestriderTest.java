package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OodSphere;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaddiLifestrider.class, SporecapSpider.class, Forest.class})
class JaddiLifestriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps the chosen creatures and gains 2 life for each")
    void entersAndGainsLifeForChosenCreatures() {
        harness.setLife(player1, 20);
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SporecapSpider());

        castJaddiLifestrider();

        Permanent jaddiLifestrider = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Jaddi Lifestrider"));
        harness.handleMultiplePermanentsChosen(player1, List.of(spider.getId(), jaddiLifestrider.getId()));

        assertThat(spider.isTapped()).isTrue();
        assertThat(jaddiLifestrider.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("ETB may tap no creatures")
    void mayTapNoCreatures() {
        harness.setLife(player1, 20);
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());

        castJaddiLifestrider();

        Permanent jaddiLifestrider = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Jaddi Lifestrider"));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(spider.isTapped()).isFalse();
        assertThat(jaddiLifestrider.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("May choose only some eligible creatures and leave Lifestrider untapped")
    void mayChooseSubsetOfCreatures() {
        harness.setLife(player1, 20);
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());

        castJaddiLifestrider();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Jaddi Lifestrider")).isTapped()).isFalse();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapped creatures, noncreatures, and opposing creatures cannot be chosen")
    void rejectsIneligiblePermanents() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        tapped.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SporecapSpider());

        castJaddiLifestrider();

        for (Permanent ineligible : List.of(tapped, land, opponent)) {
            assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ineligible.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Jaddi Lifestrider")));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        harness.assertLife(player1, 22);
    }

    @Test
    @CardUsed({OodSphere.class})
    @DisplayName("A creature prevented from becoming tapped cannot be chosen for life gain")
    void cannotChooseCreatureThatCannotBecomeTapped() {
        harness.setLife(player1, 20);
        Permanent restricted = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player2.getId();
        gd.planechase.faceUp.add(new PlanarObject(new OodSphere(), gd.nextTimestamp()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        PlanechaseService planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        TriggerCollectionService triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextETBTokenMultiTargetTrigger(gd));
        harness.handlePermanentChosen(player2, restricted.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castJaddiLifestrider();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(restricted.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Jaddi Lifestrider")));
        assertThat(restricted.isTapped()).isFalse();
        harness.assertLife(player1, 22);
    }

    private void castJaddiLifestrider() {
        harness.castFromHand(player1, new JaddiLifestrider(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

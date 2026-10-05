package com.github.laxika.magicalvibes.cards.l;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LittjaraKinseekers.class, JasperaSentinel.class, BeskirShieldmate.class,
        GoldveinPick.class, DepartTheRealm.class})
class LittjaraKinseekersTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a counter and scries when two other creatures share a type with it")
    void getsCounterAndScriesWithSharedType() {
        Card top = new GoldveinPick();
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new LittjaraKinseekers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent kinseekers = findPermanent(player1, "Littjara Kinseekers");
        assertThat(gqs.getEffectivePower(gd, kinseekers)).isEqualTo(3);
        assertThat(kinseekers.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Does not trigger for three creatures without one common creature type")
    void doesNotTriggerWithoutCommonType() {
        Card top = new GoldveinPick();
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new BeskirShieldmate());
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new LittjaraKinseekers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kinseekers = findPermanent(player1, "Littjara Kinseekers");
        assertThat(gqs.getEffectivePower(gd, kinseekers)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doesNotTriggerWithOnlyTwoCreatures() {
        harness.addToBattlefield(player1, new JasperaSentinel());
        castKinseekers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Littjara Kinseekers").getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotCountOpponentsCreatures() {
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player2, new JasperaSentinel());
        harness.addToBattlefield(player2, new JasperaSentinel());
        castKinseekers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Littjara Kinseekers").getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void skipsCounterAndScryWhenConditionStopsBeingTrue() {
        Card top = new GoldveinPick();
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.setLibrary(player1, List.of(top));
        castKinseekers();
        assertThat(gd.stack).hasSize(1);

        bounce(findPermanent(player1, "Jaspera Sentinel"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Littjara Kinseekers").getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void scriesAfterSourceLeavesIfThreeOtherCreaturesStillShareAType() {
        Card top = new GoldveinPick();
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.setLibrary(player1, List.of(top));
        castKinseekers();

        bounce(findPermanent(player1, "Littjara Kinseekers"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Littjara Kinseekers")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void extraQualifyingCreaturesGiveOnlyOneCounterAndScryOneCard() {
        Card top = new GoldveinPick();
        Card next = new BeskirShieldmate();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new JasperaSentinel());
        }
        harness.setLibrary(player1, List.of(top, next));
        castKinseekers();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Littjara Kinseekers").getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsCounterOnSourceEvenWithEmptyLibrary() {
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.setLibrary(player1, List.of());
        castKinseekers();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Littjara Kinseekers").getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castKinseekers() {
        harness.setHand(player1, List.of(new LittjaraKinseekers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void bounce(Permanent target) {
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GurmagAngler;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.n.NoxiousRevival;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnderworldBreach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({MurktideRegent.class, Shock.class, LavaAxe.class, GrizzlyBears.class, Forest.class, NoxiousRevival.class, GurmagAngler.class, Ponder.class, UnderworldBreach.class})
class MurktideRegentTest extends BaseCardTest {

    @Test
    void entersWithCountersForExiledInstantsAndSorceries() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new Shock(), new LavaAxe(), new GrizzlyBears(), new Forest(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsACounterForEachInstantOrSorceryLeavingGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new Shock(), new LavaAxe(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new MurktideRegent(), new NoxiousRevival(),
                new NoxiousRevival()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        UUID shockId = gd.playerGraveyards.get(player1.getId()).get(0).getId();
        harness.castInstant(player1, 0, shockId);
        resolveAllTriggers();

        UUID lavaAxeId = gd.playerGraveyards.get(player1.getId()).get(0).getId();
        harness.castInstant(player1, 0, lavaAxeId);
        resolveAllTriggers();

        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
    @Test
    @DisplayName("Enters with a counter for each delved instant or sorcery")
    void entersWithCountersForDelvedInstantsAndSorceries() {
        List<Card> graveyard = List.of(
                new Shock(), new Ponder(), new GrizzlyBears(), new Shock(), new Ponder());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        assertThat(murktide).isNotNull();
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets a counter for each instant or sorcery card that leaves its controller's graveyard")
    void getsCountersWhenInstantOrSorceryCardsLeaveGraveyard() {
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(
                new Shock(), new Ponder(), new GrizzlyBears(), new Shock(), new Ponder()));
        harness.setHand(player1, List.of(new GurmagAngler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        resolveAllTriggers();

        Permanent murktide = findPermanent(player1, "Murktide Regent");
        assertThat(murktide).isNotNull();
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void entersWithoutCountersWhenNoCardsAreDelved() {
        harness.setGraveyard(player1, List.of(new Ponder(), new NoxiousRevival()));
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Murktide Regent")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void existingRegentTriggersBeforeSecondRegentResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MurktideRegent());
        harness.setGraveyard(player1, List.of(new Ponder(), new NoxiousRevival()));
        harness.setHand(player1, List.of(new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Murktide Regent")).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Murktide Regent")).hasSize(2)
                .allSatisfy(regent -> assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void opponentGraveyardDepartureDoesNotTrigger() {
        Permanent murktide = harness.addToBattlefieldAndReturn(player1, new MurktideRegent());
        Ponder ponder = new Ponder();
        harness.setGraveyard(player2, List.of(ponder));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, ponder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landLeavingControllerGraveyardDoesNotTrigger() {
        Permanent murktide = harness.addToBattlefieldAndReturn(player1, new MurktideRegent());
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.stack).isEmpty();
        assertThat(murktide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapeExilePaymentIsSeparateFromOptionalDelve() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(
                new Ponder(), new NoxiousRevival(), new Forest(), new MurktideRegent()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castFromGraveyard(player1, 3, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Murktide Regent")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TormodTheDesecrator.class, Recollect.class, Reminisce.class,
        GrizzlyBears.class, Shock.class})
class TormodTheDesecratorTest extends BaseCardTest {

    @Test
    void createsTappedZombieWhenAnyCardLeavesYourGraveyard() {
        addReadyTormod();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, shock.getId());
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    void createsOnlyOneZombieWhenSeveralCardsLeaveTogether() {
        addReadyTormod();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenCardIsPutIntoYourGraveyard() {
        addReadyTormod();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenCardsLeaveOpponentsGraveyard() {
        addReadyTormod();
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenEmptyGraveyardIsShuffled() {
        addReadyTormod();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsZombieForEachSeparateGraveyardDeparture() {
        addReadyTormod();
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Recollect(), new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2)
                .allSatisfy(zombie -> assertThat(zombie.isTapped()).isTrue());
    }

    @Test
    void queuedTriggerStillCreatesZombieAfterTormodDies() {
        addReadyTormod();
        Shock returnedCard = new Shock();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Tormod, the Desecrator"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Tormod, the Desecrator")).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie").isTapped()).isTrue();
    }

    private void addReadyTormod() {
        harness.addToBattlefield(player1, new TormodTheDesecrator());
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElendaTheDuskRose.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class ElendaTheDuskRoseTest extends BaseCardTest {

    @Test
    void createsLifelinkVampireTokensEqualToPowerWhenItDies() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killPermanent(player2, "Grizzly Bears");

        Permanent elenda = findPermanent(player1, "Elenda, the Dusk Rose");
        assertThat(elenda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(2);

        killPermanent(player1, "Elenda, the Dusk Rose");

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    @Test
    void getsACounterWhenAnotherCreatureItsControllerControlsDies() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killPermanent(player1, "Grizzly Bears");

        Permanent elenda = findPermanent(player1, "Elenda, the Dusk Rose");
        assertThat(elenda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    void createsOneTokenWhenItDiesWithoutOtherCreaturesDying() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());

        killPermanent(player1, "Elenda, the Dusk Rose");

        harness.assertInGraveyard(player1, "Elenda, the Dusk Rose");
        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
        assertThat(findPermanents(player2, "Vampire")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousDeathsDoNotIncreaseTheNumberOfTokens() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elenda, the Dusk Rose");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
        assertThat(findPermanents(player2, "Vampire")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathUsesPowerBeforeAnUnresolvedCounterTrigger() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanent(player1, "Elenda, the Dusk Rose")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Elenda, the Dusk Rose"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
        harness.assertInGraveyard(player1, "Elenda, the Dusk Rose");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createdVampireGainsLifeWhenItDealsCombatDamage() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        killPermanent(player1, "Elenda, the Dusk Rose");
        Permanent vampire = findPermanent(player1, "Vampire");
        vampire.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertLife(player2, 19);
    }

    private void killPermanent(Player controller, String name) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID permanentId = harness.getPermanentId(controller, name);
        harness.castAndResolveInstant(player2, 0, permanentId);
        harness.passBothPriorities();
    }
}

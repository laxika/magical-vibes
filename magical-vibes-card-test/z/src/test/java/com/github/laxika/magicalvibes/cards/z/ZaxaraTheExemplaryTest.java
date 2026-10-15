package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaxaraTheExemplary.class, Hurricane.class, GrizzlyBears.class, JinnieFayJetmirsSecond.class})
class ZaxaraTheExemplaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds two mana of one chosen color")
    void addsTwoManaOfChosenColor() {
        Permanent zaxara = addCreatureReady(player1, new ZaxaraTheExemplary());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(zaxara.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an X spell creates a Hydra with X plus-one-plus-one counters")
    void xSpellCreatesHydraWithXCounters() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        Permanent hydra = findPermanent(player1, "Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(3);
        assertThat(hydra.getCard().getSubtypes()).contains(CardSubtype.HYDRA);
    }

    @Test
    @DisplayName("Casting a spell without X does not create a Hydra")
    void nonXSpellDoesNotCreateHydra() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hydra")).isZero();
    }

    @Test
    void zeroXHydraDiesAfterTriggerResolves() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Hydra");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentXSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        harness.assertNotOnBattlefield(player1, "Hydra");
        harness.assertNotOnBattlefield(player2, "Hydra");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hydraIsCreatedBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(findPermanent(player1, "Hydra").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void replacementCatStillReceivesXCounters() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleListChoice(player1, "Cat");

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(cat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Hydra");
    }

    @Test
    void decliningTokenReplacementStillPutsCountersOnHydra() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleListChoice(player1, "Original tokens");

        Permanent hydra = findPermanent(player1, "Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }
}

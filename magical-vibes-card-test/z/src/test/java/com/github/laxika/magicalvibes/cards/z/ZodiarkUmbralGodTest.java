package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GaiusVanBaelsar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheScarabGod;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZodiarkUmbralGod.class, GrizzlyBears.class, TheScarabGod.class, CruelEdict.class,
        TreetopVillage.class, GaiusVanBaelsar.class})
class ZodiarkUmbralGodTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices half their non-God creatures, rounded down")
    void eachPlayerSacrificesHalfNonGodCreaturesRoundedDown() {
        Permanent god = harness.addToBattlefieldAndReturn(player1, new TheScarabGod());
        List<Permanent> player1Bears = addBears(player1, 3);
        addBears(player2, 2);
        harness.setHand(player1, List.of(new ZodiarkUmbralGod()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Bears.getFirst().getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(findPermanents(player2, "Grizzly Bears").getFirst().getId()));

        resolveAllTriggers();

        Permanent zodiark = findPermanent(player1, "Zodiark, Umbral God");
        assertThat(findPermanent(player1, "The Scarab God")).isSameAs(god);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(zodiark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature sacrifice puts a +1/+1 counter on Zodiark")
    void creatureSacrificePutsCounterOnZodiark() {
        Permanent zodiark = addCreatureReady(player1, new ZodiarkUmbralGod());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(zodiark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("One non-God creature per player requires no sacrifices")
    void oneNonGodCreaturePerPlayerRequiresNoSacrifices() {
        addBears(player1, 1);
        addBears(player2, 1);
        harness.setHand(player1, List.of(new ZodiarkUmbralGod()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(findPermanent(player1, "Zodiark, Umbral God")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing another God also puts a counter on Zodiark")
    void sacrificingAnotherGodPutsCounterOnZodiark() {
        Permanent zodiark = harness.addToBattlefieldAndReturn(player1, new ZodiarkUmbralGod());
        harness.addToBattlefield(player1, new TheScarabGod());
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handlePermanentChosen(player1, findPermanent(player1, "The Scarab God").getId());
        resolveAllTriggers();

        assertThat(zodiark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zodiark does not trigger for its own sacrifice")
    void doesNotTriggerForItsOwnSacrifice() {
        harness.addToBattlefield(player2, new ZodiarkUmbralGod());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(countPermanents(player2, "Zodiark, Umbral God")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When sacrificed simultaneously, Zodiark triggers only for other creatures")
    void simultaneousSacrificeTriggersOnlyForOtherCreatures() {
        Permanent zodiark = harness.addToBattlefieldAndReturn(player2, new ZodiarkUmbralGod());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GaiusVanBaelsar()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, 1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(zodiark.getId()));

        assertThat(countPermanents(player2, "Zodiark, Umbral God")).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Sacrificing an animated land puts a counter on Zodiark")
    void sacrificingAnimatedLandPutsCounterOnZodiark() {
        Permanent zodiark = harness.addToBattlefieldAndReturn(player1, new ZodiarkUmbralGod());
        Permanent village = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, village)).isTrue();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treetop Village")).isZero();
        assertThat(zodiark.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private List<Permanent> addBears(Player player, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> harness.addToBattlefieldAndReturn(player, new GrizzlyBears()))
                .toList();
    }
}

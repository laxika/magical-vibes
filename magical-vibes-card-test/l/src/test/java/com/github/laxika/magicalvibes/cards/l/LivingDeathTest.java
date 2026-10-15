package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AjanisWelcome;
import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DesecratedTomb;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PoisonTipArcher;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingDeath.class, CanopySpider.class, DesecratedTomb.class, LowlandGiant.class,
        Mountain.class, TrainedArmodon.class, WindDrake.class, AjanisWelcome.class, PoisonTipArcher.class,
        AlexiosDeimosOfKosmos.class})
class LivingDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's graveyard creatures replace the creatures they control")
    void swapsGraveyardsWithBattlefields() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new WindDrake());
        harness.setGraveyard(player1, List.of(new LowlandGiant()));
        harness.setGraveyard(player2, List.of(new CanopySpider()));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertOnBattlefield(player2, "Canopy Spider");
        harness.assertNotOnBattlefield(player1, "Trained Armodon");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Creatures sacrificed to Living Death are not reanimated by it")
    void sacrificedCreaturesStayInTheGraveyard() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent armodon = addCreatureReady(player1, new TrainedArmodon());

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(armodon.getId()));
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertNotOnBattlefield(player1, "Trained Armodon");
    }

    @Test
    @DisplayName("Noncreature cards stay in the graveyard")
    void leavesNoncreatureCardsAlone() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setGraveyard(player1, List.of(new Mountain(), new LowlandGiant()));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolves with empty graveyards and empty battlefields")
    void resolvesWithNothingToDo() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Living Death");
    }

    @Test
    @DisplayName("Several creature cards leaving together cause one graveyard-leave trigger")
    void batchesCreatureCardsLeavingGraveyardTriggers() {
        harness.addToBattlefield(player1, new DesecratedTomb());
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setGraveyard(player1, List.of(new TrainedArmodon(), new LowlandGiant()));

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("A surviving enchantment triggers for each returned creature its controller receives")
    void survivingEntryWatcherSeesEachReturnedCreature() {
        harness.addToBattlefield(player1, new AjanisWelcome());
        harness.setGraveyard(player1, List.of(new LowlandGiant(), new TrainedArmodon()));
        harness.setGraveyard(player2, List.of(new CanopySpider()));

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Ajani's Welcome");
    }

    @Test
    @DisplayName("A sacrificed death watcher sees other creatures dying simultaneously on both sides")
    void sacrificedWatcherSeesAllOtherSimultaneousDeaths() {
        addCreatureReady(player1, new PoisonTipArcher());
        addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new WindDrake());

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Poison-Tip Archer");
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertInGraveyard(player2, "Wind Drake");
    }

    @Test
    @DisplayName("A returning death watcher does not see the earlier sacrifice step")
    void returningWatcherDoesNotSeeEarlierDeaths() {
        harness.setGraveyard(player1, List.of(new PoisonTipArcher()));
        addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new WindDrake());

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Poison-Tip Archer");
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertInGraveyard(player2, "Wind Drake");
    }

    @Test
    @CardUsed({AlexiosDeimosOfKosmos.class})
    @DisplayName("Creatures that cannot be sacrificed survive while the rest of the spell resolves")
    void cannotBeSacrificedCreatureSurvives() {
        harness.addToBattlefield(player2, new AlexiosDeimosOfKosmos());
        addCreatureReady(player2, new WindDrake());
        harness.setGraveyard(player1, List.of(new LowlandGiant()));
        harness.setGraveyard(player2, List.of(new CanopySpider()));

        harness.castFromHand(player1, new LivingDeath(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alexios, Deimos of Kosmos");
        harness.assertNotInGraveyard(player2, "Alexios, Deimos of Kosmos");
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertOnBattlefield(player2, "Canopy Spider");
    }
}

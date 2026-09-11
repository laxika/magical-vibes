package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DesecratedTomb;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
        Mountain.class, TrainedArmodon.class, WindDrake.class})
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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }
}

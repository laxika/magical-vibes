package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingDeath.class, GrayOgre.class, GrizzlyBears.class, HillGiant.class,
        Mountain.class, SavannahLions.class})
class LivingDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's graveyard creatures replace the creatures they control")
    void swapsGraveyardsWithBattlefields() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new SavannahLions());
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setGraveyard(player2, List.of(new GrayOgre()));

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Gray Ogre");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Savannah Lions");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Creatures sacrificed to Living Death are not reanimated by it")
    void sacrificedCreaturesStayInTheGraveyard() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature cards stay in the graveyard")
    void leavesNoncreatureCardsAlone() {
        harness.setHand(player1, List.of(new LivingDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setGraveyard(player1, List.of(new Mountain(), new HillGiant()));

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
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
}

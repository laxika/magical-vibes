package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AerithRescueMission;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CidTimelessArtificer.class, AerithRescueMission.class, GrizzlyBears.class, Ornithopter.class})
class CidTimelessArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Cid boosts artifact creatures and Heroes by Artificers on the battlefield and in the graveyard")
    void boostsArtifactCreaturesAndHeroes() {
        harness.addToBattlefield(player1, new CidTimelessArtificer());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CidTimelessArtificer()));

        harness.setHand(player1, List.of(new AerithRescueMission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        Permanent hero = findPermanents(player1, "Hero").getFirst();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling Cid draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CidTimelessArtificer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cid, Timeless Artificer");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.t.TerraStomper;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummonersBane.class, GrizzlyBears.class, Millstone.class, KrakenHatchling.class, Cancel.class, TerraStomper.class})
class SummonersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and creates a 2/2 blue Illusion")
    void countersCreatureSpellAndCreatesIllusion() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SummonersBane()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        List<Permanent> illusions = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Illusion"))
                .toList();
        assertThat(illusions).hasSize(1);
        Permanent illusion = illusions.getFirst();
        assertThat(illusion.getCard().getPower()).isEqualTo(2);
        assertThat(illusion.getCard().getToughness()).isEqualTo(2);
        assertThat(illusion.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(illusion.getCard().getSubtypes()).contains(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SummonersBane()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates an Illusion even when the creature spell cannot be countered")
    void createsIllusionWhenCreatureCannotBeCountered() {
        TerraStomper stomper = new TerraStomper();
        harness.setHand(player1, List.of(stomper));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new SummonersBane()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, stomper.getId());

        harness.assertOnBattlefield(player2, "Illusion");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Terra Stomper");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Terra Stomper");
    }

    @Test
    @DisplayName("Does not create an Illusion when the target leaves the stack")
    void noIllusionWhenTargetLeavesStack() {
        KrakenHatchling hatchling = new KrakenHatchling();
        harness.setHand(player1, List.of(hatchling, new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new SummonersBane()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, hatchling.getId());
        harness.assertInGraveyard(player1, "Kraken Hatchling");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Illusion");
        harness.assertInGraveyard(player2, "Summoner's Bane");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's own creature spell")
    void canCounterOwnCreatureSpell() {
        KrakenHatchling hatchling = new KrakenHatchling();
        harness.setHand(player1, List.of(hatchling, new SummonersBane()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, hatchling.getId());

        harness.assertInGraveyard(player1, "Kraken Hatchling");
        harness.assertNotOnBattlefield(player1, "Kraken Hatchling");
        harness.assertOnBattlefield(player1, "Illusion");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Illusion");
        assertThat(gd.stack).isEmpty();
    }
}

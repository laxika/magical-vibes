package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormKilnArtist.class, BarkshellBlessing.class, GrizzlyBears.class,
        Ornithopter.class, CampusGuide.class, ExpandedAnatomy.class})
class StormKilnArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact its controller controls")
    void getsPowerForControlledArtifacts() {
        Permanent artist = addCreatureReady(player1, new StormKilnArtist());
        int powerWithoutArtifacts = gqs.getEffectivePower(gd, artist);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, artist)).isEqualTo(powerWithoutArtifacts + 2);
    }

    @Test
    @DisplayName("Casting and copying an instant creates a Treasure for each magecraft trigger")
    void castingAndCopyingInstantCreatesTreasures() {
        addCreatureReady(player1, new StormKilnArtist());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Sorcery magecraft resolves before the spell and Treasure increases only power")
    void sorceryCreatesTreasureBeforeResolving() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new StormKilnArtist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        int initialPower = gqs.getEffectivePower(gd, artist);
        int initialToughness = gqs.getEffectiveToughness(gd, artist);
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, artist)).isEqualTo(initialPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, artist)).isEqualTo(initialToughness);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();

        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Each Artist triggers separately for the same sorcery")
    void multipleArtistsEachCreateTreasure() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StormKilnArtist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StormKilnArtist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        int firstPower = gqs.getEffectivePower(gd, first);
        int secondPower = gqs.getEffectivePower(gd, second);
        harness.setHand(player1, List.of(new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(firstPower + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(secondPower + 2);
    }

    @Test
    @DisplayName("Casting an artifact creature does not trigger magecraft")
    void creatureSpellDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new StormKilnArtist());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Ornithopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentsInstantDoesNotCreateTreasure() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new StormKilnArtist());
        harness.setHand(player2, List.of(new BarkshellBlessing()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, artist.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }
}

package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthshakerDreadmaw.class, Forest.class, GrizzlyBears.class, PygmyAllosaurus.class})
class EarthshakerDreadmawTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each other Dinosaur you control")
    void drawsForEachOtherDinosaurYouControl() {
        harness.addToBattlefield(player1, new PygmyAllosaurus());
        harness.addToBattlefield(player1, new PygmyAllosaurus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PygmyAllosaurus());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EarthshakerDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw when no other Dinosaur is controlled")
    void doesNotDrawWithoutOtherDinosaur() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new EarthshakerDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts another Earthshaker Dreadmaw added before the trigger resolves")
    void countsDinosaursAtResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EarthshakerDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.addToBattlefield(player1, new EarthshakerDreadmaw());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not count a Dinosaur that left before the trigger resolves")
    void doesNotCountDepartedDinosaur() {
        var other = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new EarthshakerDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(other);
        gd.playerGraveyards.get(player1.getId()).add(other.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trigger still draws for other Dinosaurs after its source leaves")
    void drawsAfterSourceLeaves() {
        var other = harness.addToBattlefieldAndReturn(player1, new EarthshakerDreadmaw());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EarthshakerDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}

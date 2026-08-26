package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChocoboRacetrack;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GysahlGreens;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TravelingChocobo.class, ChocoboRacetrack.class, Forest.class,
        GysahlGreens.class, GrizzlyBears.class, SoulWarden.class
})
class TravelingChocoboTest extends BaseCardTest {

    @Test
    void playsLandFromTopOfLibrary() {
        harness.addToBattlefield(player1, new TravelingChocobo());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castsBirdFromTopOfLibrary() {
        harness.addToBattlefield(player1, new TravelingChocobo());
        TravelingChocobo bird = new TravelingChocobo();
        harness.setLibrary(player1, List.of(bird));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof TravelingChocobo)
                .hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotCastNonBirdFromTopOfLibrary() {
        harness.addToBattlefield(player1, new TravelingChocobo());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublesLandfallTrigger() {
        harness.addToBattlefield(player1, new TravelingChocobo());
        harness.addToBattlefield(player1, new ChocoboRacetrack());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void doublesTriggerCausedByBirdEntering() {
        harness.addToBattlefield(player1, new TravelingChocobo());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new GysahlGreens()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }
}

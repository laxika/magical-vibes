package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Worldweave.class, Forest.class, GrizzlyBears.class, Shock.class})
class WorldweaveTest extends BaseCardTest {

    @Test
    void putsASeekingLandOntoTheBattlefieldTappedWhenCastingACreature() {
        Forest soughtLand = new Forest();
        GrizzlyBears otherLibraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(otherLibraryCard, soughtLand));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == soughtLand && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
    }

    @Test
    void doesNotTriggerForNoncreatureSpells() {
        Forest land = new Forest();
        harness.addToBattlefield(player1, new Worldweave());
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }
}

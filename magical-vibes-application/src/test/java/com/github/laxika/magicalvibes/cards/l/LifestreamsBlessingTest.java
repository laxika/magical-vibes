package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifestreamsBlessing.class, HillGiant.class, GrizzlyBears.class,
        Forest.class, Island.class, Mountain.class})
class LifestreamsBlessingTest extends BaseCardTest {

    @Test
    void drawsCardsEqualToGreatestControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        List<Card> library = List.of(new Forest(), new Island(), new Mountain());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new LifestreamsBlessing()));
        addNormalMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertLife(player1, 20);
    }

    @Test
    void foretellCastDrawsAndGainsTwiceTheGreatestPower() {
        List<Card> library = List.of(new Forest(), new Island(), new Mountain());
        LifestreamsBlessing blessing = new LifestreamsBlessing();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(blessing));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, blessing.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertLife(player1, 26);
    }

    @Test
    void usesGreatestPowerAsItWasWhenCast() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        List<Card> library = List.of(new Forest(), new Island(), new Mountain());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new LifestreamsBlessing()));
        addNormalMana();

        harness.castInstant(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(hillGiant);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}

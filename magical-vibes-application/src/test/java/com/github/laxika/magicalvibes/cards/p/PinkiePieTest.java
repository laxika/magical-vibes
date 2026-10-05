package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArchpriestOfIona;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PinkiePie.class, ArchpriestOfIona.class, Shock.class, GrizzlyBears.class})
class PinkiePieTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped Treasure when its controller casts a smiling Pinkie Pie")
    void createsTappedTreasureOnSpellCast() {
        harness.addToBattlefield(player1, new PinkiePie());
        harness.setHand(player1, java.util.List.of(new PinkiePie()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Makes each controlled creature a party member and always has a full party")
    void makesPartyAlwaysFull() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new PinkiePie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(2);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Party membership does not grant unrelated creature types")
    void partyMembershipDoesNotGrantCreatureTypes() {
        Permanent pinkie = harness.addToBattlefieldAndReturn(player1, new PinkiePie());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.effectiveCreatureSubtypes(gd, pinkie)).doesNotContain(CardSubtype.ELF);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).doesNotContain(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Every controlled creature counts in the party, even beyond four")
    void partyIncludesMoreThanFourCreatures() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new PinkiePie());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent casting a spell does not create Treasure")
    void opponentSpellDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new PinkiePie());
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertLife(player1, 18);
    }
}

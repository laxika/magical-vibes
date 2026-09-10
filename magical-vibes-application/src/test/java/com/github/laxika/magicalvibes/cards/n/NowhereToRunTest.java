package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.f.FrostTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SireOfSevenDeaths;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NowhereToRun.class, GrizzlyBears.class, CarnageTyrant.class, Shock.class,
        SireOfSevenDeaths.class, FrostTitan.class})
class NowhereToRunTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability gives an opponent's creature -3/-3")
    void weakensTargetCreatureWhenItEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Its controller can target an opponent's hexproof creature")
    void controllerCanTargetOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, tyrant.getId());
        harness.passBothPriorities();

        assertThat(tyrant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("It prevents ward from triggering on an opponent's creature")
    void preventsOpponentCreatureWardTrigger() {
        harness.addToBattlefield(player1, new NowhereToRun());
        SireOfSevenDeaths sireCard = new SireOfSevenDeaths();
        sireCard.setKeywords(Set.of(Keyword.WARD));
        Permanent sire = harness.addToBattlefieldAndReturn(player2, sireCard);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sire.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sire.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("It does not suppress unrelated counter-unless target triggers")
    void doesNotSuppressNonWardCounterTrigger() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new FrostTitan());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, titan.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(titan.getMarkedDamage()).isZero();
    }
}

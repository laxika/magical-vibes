package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.d.DwarvenBloodboiler;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalthorTheDefiled.class, CabalTrainee.class, DwarvenBloodboiler.class, GiantWarthog.class, KrosanVerge.class, Xenograft.class})
class BalthorTheDefiledTest extends BaseCardTest {

    @Test
    @DisplayName("Minion creatures get +1/+1, while other creatures do not")
    void boostsMinionsOnly() {
        Permanent balthor = addCreatureReady(player1, new BalthorTheDefiled());
        Permanent ownMinion = addCreatureReady(player1, new CabalTrainee());
        Permanent opposingMinion = addCreatureReady(player2, new CabalTrainee());
        Permanent nonMinion = addCreatureReady(player1, new GiantWarthog());

        assertThat(gqs.getEffectivePower(gd, balthor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, balthor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownMinion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownMinion)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingMinion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingMinion)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonMinion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nonMinion)).isEqualTo(5);
    }

    @Test
    @CardUsed(Xenograft.class)
    @DisplayName("The anthem also applies if Balthor becomes a Minion")
    void boostsItselfWhenItBecomesAMinion() {
        Permanent balthor = addCreatureReady(player1, new BalthorTheDefiled());
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.MINION.name());

        assertThat(gqs.getEffectivePower(gd, balthor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, balthor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiling Balthor returns each player's black and red creature cards")
    void returnsBlackAndRedCreaturesFromEachGraveyard() {
        harness.addToBattlefield(player1, new BalthorTheDefiled());
        harness.setGraveyard(player1, List.of(
                new CabalTrainee(), new DwarvenBloodboiler(), new GiantWarthog(), new KrosanVerge()));
        harness.setGraveyard(player2, List.of(
                new CabalTrainee(), new DwarvenBloodboiler(), new GiantWarthog(), new KrosanVerge()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Balthor the Defiled");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cabal Trainee");
        harness.assertOnBattlefield(player1, "Dwarven Bloodboiler");
        harness.assertInGraveyard(player1, "Giant Warthog");
        harness.assertInGraveyard(player1, "Krosan Verge");
        harness.assertOnBattlefield(player2, "Cabal Trainee");
        harness.assertOnBattlefield(player2, "Dwarven Bloodboiler");
        harness.assertInGraveyard(player2, "Giant Warthog");
        harness.assertInGraveyard(player2, "Krosan Verge");
    }

    @Test
    @DisplayName("The Minion bonus also applies to Balthor if it becomes a Minion")
    void boostsBalthorIfItBecomesAMinion() {
        Permanent balthor = harness.addToBattlefieldAndReturn(player1, new BalthorTheDefiled());
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.MINION);

        assertThat(gqs.getEffectivePower(gd, balthor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, balthor)).isEqualTo(3);
    }
}

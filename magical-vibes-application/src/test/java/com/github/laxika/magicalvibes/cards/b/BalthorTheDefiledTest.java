package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianProcessor;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
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

@CardUsed({BalthorTheDefiled.class, GrizzlyBears.class, PhyrexianProcessor.class,
        RagingGoblin.class, ScatheZombies.class, DarkRitual.class, Xenograft.class})
class BalthorTheDefiledTest extends BaseCardTest {

    @Test
    @DisplayName("Minion creatures get +1/+1, while other creatures do not")
    void boostsMinionsOnly() {
        harness.addToBattlefield(player1, new BalthorTheDefiled());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "5");

        Permanent processor = findPermanent(player1, "Phyrexian Processor");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(processor), 0, null, null);
        harness.passBothPriorities();

        Permanent minion = findPermanent(player1, "Phyrexian Minion");
        assertThat(gqs.getEffectivePower(gd, minion)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, minion)).isEqualTo(6);

        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
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

    @Test
    @DisplayName("Exiling Balthor returns each player's black and red creature cards")
    void returnsBlackAndRedCreaturesFromEachGraveyard() {
        harness.addToBattlefield(player1, new BalthorTheDefiled());
        harness.setGraveyard(player1, List.of(
                new ScatheZombies(), new RagingGoblin(), new GrizzlyBears(), new DarkRitual()));
        harness.setGraveyard(player2, List.of(
                new ScatheZombies(), new RagingGoblin(), new GrizzlyBears(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Balthor the Defiled");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scathe Zombies");
        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dark Ritual");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
        harness.assertOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dark Ritual");
    }
}

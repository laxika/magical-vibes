package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DayOfTheDragons.class, Forest.class, GrizzlyBears.class, ShivanDragon.class})
class DayOfTheDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles your creatures and creates a 5/5 flying Dragon for each")
    void exilesYourCreaturesAndCreatesDragons() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolveDayOfTheDragons();

        harness.assertOnBattlefield(player1, "Day of the Dragons");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        List<Permanent> dragons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(dragons).hasSize(2);
        assertThat(dragons).allSatisfy(dragon -> {
            assertThat(dragon.getEffectivePower()).isEqualTo(5);
            assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
            assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(dragon.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.FLYING);
        });

        Permanent day = findPermanent(player1, "Day of the Dragons");
        assertThat(gd.getCardsExiledByPermanent(day.getId())).hasSize(2);
    }

    @Test
    @DisplayName("When it leaves, sacrifices your Dragons and returns its exiled creatures")
    void leavesBySacrificingDragonsAndReturningExiledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAndResolveDayOfTheDragons();

        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new ShivanDragon());
        Permanent day = findPermanent(player1, "Day of the Dragons");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, day));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Day of the Dragons");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        harness.assertOnBattlefield(player2, "Shivan Dragon");
        harness.assertInGraveyard(player1, "Day of the Dragons");
        harness.assertInGraveyard(player1, "Shivan Dragon");
        assertThat(gd.getCardsExiledByPermanent(day.getId())).isEmpty();
    }

    private void castAndResolveDayOfTheDragons() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DayOfTheDragons()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}

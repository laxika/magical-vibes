package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.n.NobleTemplar;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DayOfTheDragons.class, ControlMagic.class, DragonMage.class, NobleTemplar.class,
        TempleOfTheFalseGod.class})
class DayOfTheDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles your creatures and creates a 5/5 flying Dragon for each")
    void exilesYourCreaturesAndCreatesDragons() {
        harness.addToBattlefield(player1, new NobleTemplar());
        harness.addToBattlefield(player1, new NobleTemplar());
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.addToBattlefield(player2, new NobleTemplar());

        castAndResolveDayOfTheDragons();

        harness.assertOnBattlefield(player1, "Day of the Dragons");
        harness.assertNotOnBattlefield(player1, "Noble Templar");
        harness.assertOnBattlefield(player1, "Temple of the False God");
        harness.assertOnBattlefield(player2, "Noble Templar");

        List<Permanent> dragons = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(dragons).hasSize(2);
        assertThat(dragons).allSatisfy(dragon -> {
            assertThat(dragon.getCard().getName()).isEqualTo("Dragon");
            assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
            assertThat(dragon.getEffectivePower()).isEqualTo(5);
            assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
            assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        });

        Permanent day = findPermanent(player1, "Day of the Dragons");
        assertThat(gd.getCardsExiledByPermanent(day.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Creates no Dragons when you control no creatures")
    void createsNoDragonsWhenYouControlNoCreatures() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.addToBattlefield(player2, new NobleTemplar());

        castAndResolveDayOfTheDragons();

        harness.assertOnBattlefield(player1, "Day of the Dragons");
        harness.assertOnBattlefield(player1, "Temple of the False God");
        harness.assertOnBattlefield(player2, "Noble Templar");
        assertThat(findPermanents(player1, "Dragon")).isEmpty();

        Permanent day = findPermanent(player1, "Day of the Dragons");
        assertThat(gd.getCardsExiledByPermanent(day.getId())).isEmpty();
    }

    @Test
    @DisplayName("When it leaves, sacrifices your Dragons and returns its exiled creatures")
    void leavesBySacrificingDragonsAndReturningExiledCreatures() {
        harness.addToBattlefield(player1, new NobleTemplar());
        castAndResolveDayOfTheDragons();

        harness.addToBattlefield(player1, new DragonMage());
        harness.addToBattlefield(player2, new DragonMage());
        Permanent day = findPermanent(player1, "Day of the Dragons");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, day));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Day of the Dragons");
        harness.assertOnBattlefield(player1, "Noble Templar");
        harness.assertNotOnBattlefield(player1, "Dragon Mage");
        harness.assertOnBattlefield(player2, "Dragon Mage");
        harness.assertNotOnBattlefield(player1, "Dragon");
        harness.assertInGraveyard(player1, "Day of the Dragons");
        harness.assertInGraveyard(player1, "Dragon Mage");
        assertThat(gd.getCardsExiledByPermanent(day.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns an exiled creature under Day of the Dragons' controller's control")
    void returnsExiledCreatureUnderItsControllersControl() {
        harness.addToBattlefield(player2, new NobleTemplar());
        Permanent creature = findPermanent(player2, "Noble Templar");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Noble Templar");
        harness.assertNotOnBattlefield(player2, "Noble Templar");

        castAndResolveDayOfTheDragons();

        Permanent day = findPermanent(player1, "Day of the Dragons");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, day));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Noble Templar");
        harness.assertNotOnBattlefield(player2, "Noble Templar");
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

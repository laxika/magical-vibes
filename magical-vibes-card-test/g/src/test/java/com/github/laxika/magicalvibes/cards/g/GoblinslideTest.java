package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.w.WeaveFate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Goblinslide.class, WeaveFate.class, AlpineGrizzly.class})
class GoblinslideTest extends BaseCardTest {

    @Test
    void payingCreatesAHastyGoblinTokenForANoncreatureSpell() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Goblin"))
                .hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void decliningDoesNotCreateAToken() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin");
    }

    @Test
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.castFromHand(player1, new AlpineGrizzly(), "{2}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Goblin");
    }

    @Test
    void paymentChoiceWaitsUntilTriggeredAbilityResolves() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Goblin");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Goblin");
    }

    @Test
    void acceptingWithoutManaDoesNotCreateAToken() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.castFromHand(player1, new WeaveFate(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Goblin");
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Goblinslide());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player2, new WeaveFate(), "{3}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Goblin");
    }
}

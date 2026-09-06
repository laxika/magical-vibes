package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinessaZephyrMage.class, GrizzlyBears.class, DarksteelRelic.class, GaeasAnthem.class, Forest.class})
class LinessaZephyrMageTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability returns a creature with mana value X")
    void returnsCreatureWithManaValueX() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(false);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 2, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(linessa.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a creature whose mana value is not X")
    void rejectsCreatureWithDifferentManaValue() {
        Permanent linessa = harness.addToBattlefieldAndReturn(player1, new LinessaZephyrMage());
        linessa.setSummoningSick(false);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("Grandeur returns a creature, artifact, enchantment, and land controlled by the target player")
    void grandeurReturnsEachPermanentType() {
        harness.addToBattlefield(player1, new LinessaZephyrMage());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GaeasAnthem());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LinessaZephyrMage()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, bear.getId());
        harness.handlePermanentChosen(player2, relic.getId());
        harness.handlePermanentChosen(player2, anthem.getId());
        harness.handlePermanentChosen(player2, forest.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(bear, relic, anthem, forest);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Darksteel Relic");
        harness.assertInHand(player2, "Gaea's Anthem");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Linessa, Zephyr Mage"));
    }
}

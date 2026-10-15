package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrogskolCavalry.class, CallousDeceiver.class, GrizzlyBears.class})
class DrogskolCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when another Spirit you control enters")
    void gainsLifeWhenAnotherSpiritEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DrogskolCavalry());
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not gain life when a non-Spirit creature enters")
    void doesNotGainLifeWhenNonSpiritEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DrogskolCavalry());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creates a 1/1 white flying Spirit token")
    void activatedAbilityCreatesSpiritToken() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new DrogskolCavalry());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(cavalry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotGainLifeForItself() {
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new DrogskolCavalry());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Spirit")
    void doesNotGainLifeForOpponentsSpirit() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DrogskolCavalry());

        harness.enterBattlefieldAndReturn(player2, new DrogskolCavalry());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Token entry gains life only when the triggered ability resolves")
    void tokenCreationTriggersLifeGainAndCanBeRepeated() {
        harness.setLife(player1, 20);
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new DrogskolCavalry());
        cavalry.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            assertThat(findPermanents(player1, "Spirit")).hasSize(activation);
            harness.passBothPriorities();

            assertThat(findPermanents(player1, "Spirit")).hasSize(activation + 1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20 + 2 * activation);
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22 + 2 * activation);
            assertThat(gd.stack).isEmpty();
        }
        assertThat(cavalry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each Cavalry triggers independently for the same Spirit token")
    void bothCavalriesGainLifeForToken() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DrogskolCavalry());
        harness.addToBattlefield(player1, new DrogskolCavalry());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.stack).isEmpty();
    }
}

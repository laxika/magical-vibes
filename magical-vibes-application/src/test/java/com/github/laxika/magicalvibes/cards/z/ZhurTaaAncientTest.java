package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MazesEnd;
import com.github.laxika.magicalvibes.cards.g.GruulCluestone;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhurTaaAncient.class, Forest.class, Mountain.class, MazesEnd.class, GruulCluestone.class, GruulGuildgate.class})
class ZhurTaaAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Controller tapping a land for mana adds one additional mana of the type it produced")
    void addsExtraManaForController() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's land also produces the additional mana")
    void addsExtraManaForOpponent() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Zhur-Taa Ancient a land produces only its normal mana")
    void noExtraWithoutZhurTaaAncient() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Ancients each add one mana immediately to the tapping player's pool")
    void multipleCopiesEachAddOneMana() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.addToBattlefield(player2, new ZhurTaaAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Guildgate produces extra mana only of its chosen type")
    void addsExtraManaOfChosenTypeForOpponent() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.enterBattlefieldAndReturn(player2, new GruulGuildgate()).untap();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "RED");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Colorless mana is also a type that receives one additional mana")
    void addsExtraColorlessMana() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.enterBattlefieldAndReturn(player1, new MazesEnd()).untap();

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("Tapping an artifact for mana does not trigger Zhur-Taa Ancient")
    void nonlandManaSourceDoesNotReceiveExtraMana() {
        harness.addToBattlefield(player1, new ZhurTaaAncient());
        harness.addToBattlefield(player1, new GruulCluestone());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }
}

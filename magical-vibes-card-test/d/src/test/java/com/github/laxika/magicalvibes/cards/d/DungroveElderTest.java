package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RootboundCrag;
import com.github.laxika.magicalvibes.cards.t.Trollhide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DungroveElder.class, Forest.class, RootboundCrag.class, Trollhide.class})
class DungroveElderTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals the number of Forests you control")
    void ptEqualsForestCount() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Is 0/0 with no Forests")
    void zeroWithoutForests() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only your Forests, not the opponent's")
    void countsOnlyControllersForests() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a Forest leaves the battlefield")
    void ptUpdatesWhenForestsChange() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(0);
    }

    @Test
    @DisplayName("A land producing green mana without the Forest subtype does not count")
    void nonForestLandDoesNotCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new RootboundCrag());
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from enchanting Dungrove Elder")
    void opponentCannotTargetElder() {
        harness.addToBattlefield(player1, new Forest());
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());
        harness.setHand(player2, List.of(new Trollhide()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, elder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Its controller can enchant it and the boost adds to its Forest count")
    void controllerCanTargetElder() {
        harness.addToBattlefield(player1, new Forest());
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new DungroveElder());
        harness.setHand(player1, List.of(new Trollhide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, elder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(4);
    }

    @Test
    @DisplayName("Its characteristic ability also defines its power and toughness in hand")
    void forestCountAppliesInHand() {
        DungroveElder elder = new DungroveElder();
        harness.setHand(player1, List.of(elder));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, elder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving without Forests puts the zero-toughness Elder into the graveyard")
    void diesWithoutForests() {
        harness.castFromHand(player1, new DungroveElder(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dungrove Elder");
        harness.assertInGraveyard(player1, "Dungrove Elder");
    }
}

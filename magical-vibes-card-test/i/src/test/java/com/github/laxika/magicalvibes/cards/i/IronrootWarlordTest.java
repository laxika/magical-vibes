package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronrootWarlord.class, GreenwoodSentinel.class})
class IronrootWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Ironroot Warlord's power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent warlord = addCreatureReady(player1, new IronrootWarlord());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ironroot Warlord creates a Soldier token without tapping")
    void createsSoldierToken() {
        Permanent warlord = addCreatureReady(player1, new IronrootWarlord());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && "Soldier".equals(permanent.getCard().getName())
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getColor() == CardColor.WHITE);
        assertThat(warlord.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick and tapped Warlord can create multiple Soldiers")
    void createsTokensWhileSummoningSickAndTapped() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new IronrootWarlord());
        warlord.setSummoningSick(true);
        warlord.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(i + 1);
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(i + 2);
            assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(i + 2);
            assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(5);
        }
        assertThat(warlord.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Warlord's power in hand and graveyard counts its owner's creatures")
    void powerIsDefinedOutsideBattlefield() {
        IronrootWarlord inHand = new IronrootWarlord();
        IronrootWarlord inGraveyard = new IronrootWarlord();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
    }
}

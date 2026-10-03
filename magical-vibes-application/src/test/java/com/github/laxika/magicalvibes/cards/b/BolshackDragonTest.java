package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BolshackDragon.class, Shock.class, Forest.class})
class BolshackDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Has no graveyard power bonus while it is not attacking")
    void noBonusWhileNotAttacking() {
        Permanent dragon = addCreatureReady(player1, new BolshackDragon());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets +1/+0 for each red card in its controller's graveyard while attacking")
    void getsRedGraveyardPowerBonusWhileAttacking() {
        Permanent dragon = addCreatureReady(player1, new BolshackDragon());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Forest()));
        declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        // Attacker declaration auto-passes through combat, so verify the boosted 8 power
        // through both double-strike damage steps rather than the cleared attacking flag.
        harness.assertLife(player2, 4);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Deals double-strike damage with an empty graveyard")
    void dealsDoubleStrikeDamageWithEmptyGraveyard() {
        Permanent dragon = addCreatureReady(player1, new BolshackDragon());
        harness.setGraveyard(player1, List.of());

        declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Ignores red cards in the opponent's graveyard while attacking")
    void ignoresOpponentsGraveyard() {
        Permanent dragon = addCreatureReady(player1, new BolshackDragon());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));

        declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Continuously updates its attack bonus as its graveyard changes")
    void updatesBonusDuringCombat() {
        Permanent dragon = addCreatureReady(player1, new BolshackDragon());
        harness.setGraveyard(player1, List.of(new Shock()));
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(7);
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(6);
        harness.setGraveyard(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);

    }
}

package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        assertThat(dragon.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(6);
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArashiTheSkyAsunder.class, ArabaMothrider.class, InnerChamberGuard.class})
class ArashiTheSkyAsunderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsXDamageToTargetCreatureWithFlying() {
        Permanent arashi = addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Araba Mothrider");
        assertThat(arashi.isTapped()).isTrue();
    }

    @Test
    void channelDealsXDamageToEachCreatureWithFlyingAndDiscardsSource() {
        harness.setHand(player1, List.of(new ArashiTheSkyAsunder()));
        addCreatureReady(player1, new ArabaMothrider());
        addCreatureReady(player2, new InnerChamberGuard());
        addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arashi, the Sky Asunder");
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertInGraveyard(player1, "Araba Mothrider");
        harness.assertInGraveyard(player2, "Araba Mothrider");
    }

    @Test
    void battlefieldAbilityAllowsZeroDamage() {
        Permanent arashi = addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Araba Mothrider");
        assertThat(arashi.isTapped()).isTrue();
    }

    @Test
    void battlefieldAbilityCannotTargetCreatureWithoutFlying() {
        addCreatureReady(player1, new ArashiTheSkyAsunder());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}

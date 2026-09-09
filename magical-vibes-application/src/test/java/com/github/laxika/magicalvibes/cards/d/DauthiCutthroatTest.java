package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SoltariVisionary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiCutthroat.class, SoltariVisionary.class, RagingGoblin.class})
class DauthiCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with shadow")
    void destroysTargetCreatureWithShadow() {
        Permanent source = addCreatureReady(player1, new DauthiCutthroat());
        Permanent target = addCreatureReady(player2, new SoltariVisionary());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Soltari Visionary");
    }

    @Test
    @DisplayName("Cannot target a creature without shadow")
    void cannotTargetCreatureWithoutShadow() {
        Permanent source = addCreatureReady(player1, new DauthiCutthroat());
        Permanent target = addCreatureReady(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with shadow");
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a shadow creature controlled by the ability's controller")
    void canTargetOwnShadowCreature() {
        addCreatureReady(player1, new DauthiCutthroat());
        Permanent target = addCreatureReady(player1, new SoltariVisionary());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soltari Visionary");
    }
}

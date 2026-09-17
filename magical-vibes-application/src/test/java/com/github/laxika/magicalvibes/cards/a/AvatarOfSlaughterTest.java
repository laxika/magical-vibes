package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarOfSlaughter.class, GrizzlyBears.class})
class AvatarOfSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have double strike")
    void allCreaturesHaveDoubleStrike() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfSlaughter());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("All creatures must attack each combat if able")
    void allCreaturesMustAttack() {
        addCreatureReady(player1, new AvatarOfSlaughter());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("An opponent's creature must attack while Avatar of Slaughter is out")
    void opposingCreatureMustAttack() {
        addCreatureReady(player1, new AvatarOfSlaughter());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A creature that cannot attack is not forced to attack")
    void creatureThatCannotAttackIsExempt() {
        addCreatureReady(player1, new AvatarOfSlaughter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player2, List.of()))
                .doesNotThrowAnyException();
    }
}

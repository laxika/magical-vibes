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

    @Test
    @DisplayName("Tapped creatures are not forced to attack")
    void tappedCreatureIsExempt() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfSlaughter());
        avatar.tap();

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Avatar of Slaughter deals damage in both combat damage steps")
    void avatarDealsDoubleStrikeDamage() {
        addCreatureReady(player1, new AvatarOfSlaughter());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 4);
    }

    @Test
    @DisplayName("An opposing creature deals damage in both combat damage steps")
    void opposingCreatureDealsDoubleStrikeDamage() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfSlaughter());
        avatar.tap();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Both global effects end when Avatar of Slaughter leaves the battlefield")
    void effectsEndWhenAvatarLeavesBattlefield() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfSlaughter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        avatar.setMarkedDamage(8);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Avatar of Slaughter");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThatCode(() -> declareAttackers(player2, List.of()))
                .doesNotThrowAnyException();
    }
}

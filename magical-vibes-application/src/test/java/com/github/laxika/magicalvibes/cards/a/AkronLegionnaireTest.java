package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkronLegionnaire.class, GrizzlyBears.class, Ornithopter.class})
class AkronLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("A non-artifact creature you control cannot attack while Akron Legionnaire is out")
    void nonArtifactCreatureCannotAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Akron Legionnaire itself can attack")
    void akronCanAttack() {
        Permanent akron = addCreatureReady(player1, new AkronLegionnaire());

        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(akron)));

        // Akron is 8/4, unblocked
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Another creature named Akron Legionnaire can attack")
    void anotherAkronLegionnaireCanAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());
        Permanent secondAkron = addCreatureReady(player1, new AkronLegionnaire());

        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(secondAkron)));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("An artifact creature you control can still attack")
    void artifactCreatureCanAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent thopter = addCreatureReady(player1, new Ornithopter());

        assertThatCode(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(thopter))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Opponent's non-artifact creatures are unaffected (restriction is controller-scoped)")
    void opponentCreatureUnaffected() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bears)));

        assertThat(bears.isAttacking()).isTrue();
    }
}

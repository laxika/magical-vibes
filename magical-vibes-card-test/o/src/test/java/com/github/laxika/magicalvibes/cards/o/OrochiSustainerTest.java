package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OrochiSustainer.class)
class OrochiSustainerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Orochi Sustainer produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = addCreatureReady(player1, new OrochiSustainer());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning sick Orochi Sustainer cannot be tapped for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new OrochiSustainer());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Orochi Sustainer's mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        addCreatureReady(player1, new OrochiSustainer());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped Orochi Sustainer cannot produce more mana")
    void cannotActivateAgainWhileTapped() {
        Permanent perm = addCreatureReady(player1, new OrochiSustainer());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Orochi Sustainer adds mana to its controller's pool during the opponent's turn")
    void addsManaToControllerOnOpponentsTurn() {
        Permanent perm = addCreatureReady(player2, new OrochiSustainer());
        harness.forceActivePlayer(player1);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isTrue();
    }
}

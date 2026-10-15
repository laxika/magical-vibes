package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FyndhornElder.class)
class FyndhornElderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Fyndhorn Elder produces two green mana")
    void tappingProducesTwoGreenMana() {
        addCreatureReady(player1, new FyndhornElder());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped Fyndhorn Elder cannot tap for mana again")
    void alreadyTappedCannotTapAgain() {
        Permanent perm = addCreatureReady(player1, new FyndhornElder());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Fyndhorn Elder cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = addCreatureReady(player1, new FyndhornElder());
        perm.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability resolves immediately into its controller's pool")
    void manaAbilityResolvesImmediatelyForController() {
        addCreatureReady(player2, new FyndhornElder());
        harness.ensurePriority(player2);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fyndhorn Elder can produce mana again after untapping")
    void canProduceManaAgainAfterUntapping() {
        Permanent perm = addCreatureReady(player1, new FyndhornElder());
        harness.tapPermanent(player1, 0);
        gd.playerManaPools.get(player1.getId()).clear();

        harness.performUntapStep(player1);
        assertThat(perm.isTapped()).isFalse();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}

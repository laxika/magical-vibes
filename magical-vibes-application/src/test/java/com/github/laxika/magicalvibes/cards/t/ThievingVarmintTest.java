package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThievingVarmint.class, GrizzlyBears.class})
class ThievingVarmintTest extends BaseCardTest {

    @Test
    void abilityAddsTwoManaOfOneColorAndCostsOneLife() {
        Permanent varmint = addCreatureReady(player1, new ThievingVarmint());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(varmint.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonOwnedSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void manaCanCastASpellOwnedByAnotherPlayer() {
        addCreatureReady(player1, new ThievingVarmint());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getNonOwnedSpellOnlyManaTotal()).isZero();
    }

    @Test
    void manaCannotCastASpellOwnedByTheController() {
        addCreatureReady(player1, new ThievingVarmint());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonOwnedSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }
}

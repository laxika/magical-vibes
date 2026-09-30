package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AninaNaturalParallelist.class, Fireball.class, GrizzlyBears.class})
class AninaNaturalParallelistTest extends BaseCardTest {

    @Test
    void castsXSpellAndConjuresCreatureWithMatchingManaValue() {
        Permanent anina = addReadyAnina();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(anina.getId()))
                .findFirst()
                .orElseThrow()
                .getCard()
                .getManaValue()).isEqualTo(1);
    }

    @Test
    void tapAbilityAddsGreenAndBlueManaRestrictedToXSpells() {
        addReadyAnina();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getXSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotCastSpellWithoutX() {
        addReadyAnina();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAnina() {
        Permanent anina = new Permanent(new AninaNaturalParallelist());
        anina.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(anina);
        return anina;
    }
}

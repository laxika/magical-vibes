package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmpressGalinaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of target legendary permanent")
    void gainsControlOfLegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Spellbook legendarySpellbook = new Spellbook();
        legendarySpellbook.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent target = harness.addToBattlefieldAndReturn(player2, legendarySpellbook);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonlegendary permanent")
    void cannotTargetNonlegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, empress), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary permanent");
    }

    private Permanent addEmpressGalina() {
        Permanent empress = harness.addToBattlefieldAndReturn(player1, new EmpressGalina());
        empress.setSummoningSick(false);
        return empress;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}

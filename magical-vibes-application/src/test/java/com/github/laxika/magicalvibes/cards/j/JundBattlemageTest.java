package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JundBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, {T}: target player loses 1 life")
    void blackAbilityMakesTargetPlayerLoseLife() {
        addBattlemageReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = harness.getGameData().getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("{G}, {T}: create a 1/1 green Saproling token")
    void greenAbilityCreatesSaprolingToken() {
        addBattlemageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.SAPROLING)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Black ability requires {B} mana")
    void blackAbilityRequiresMana() {
        addBattlemageReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBattlemageReady(Player player) {
        JundBattlemage card = new JundBattlemage();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

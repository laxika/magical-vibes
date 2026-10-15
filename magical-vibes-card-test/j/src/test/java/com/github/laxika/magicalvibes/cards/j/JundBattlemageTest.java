package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JundBattlemage.class})
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

    @Test
    void blackAbilityCanTargetItsController() {
        addBattlemageReady(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void tappingForEitherAbilityPreventsActivatingTheOther() {
        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            Permanent battlemage = addBattlemageReady(player1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.activateAbility(player1, 0, abilityIndex, null,
                    abilityIndex == 0 ? player2.getId() : null);

            assertThat(battlemage.isTapped()).isTrue();
            int otherAbilityIndex = 1 - abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, otherAbilityIndex, null,
                    otherAbilityIndex == 0 ? player2.getId() : null))
                    .isInstanceOf(IllegalStateException.class);

            harness.passBothPriorities();
            harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        }
    }

    @Test
    void summoningSicknessPreventsBothAbilities() {
        Permanent battlemage = harness.addToBattlefieldAndReturn(player1, new JundBattlemage());
        battlemage.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battlemage.isTapped()).isFalse();
    }

    @Test
    void greenAbilityRequiresGreenMana() {
        addBattlemageReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void greenAbilityResolvesAfterSourceLeavesBattlefield() {
        Permanent battlemage = addBattlemageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(battlemage);
        harness.getGameData().playerGraveyards.get(player1.getId()).add(battlemage.getCard());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addBattlemageReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JundBattlemage());
        perm.setSummoningSick(false);
        return perm;
    }
}

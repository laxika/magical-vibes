package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.cards.m.MilitiasPride;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfCloverdell.class, KnightOfMeadowgrain.class, MilitiasPride.class})
class GuardianOfCloverdellTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three 1/1 white Kithkin Soldier tokens")
    void etbCreatesThreeKithkinSoldierTokens() {
        castAndResolveGuardian();

        // Guardian + three tokens
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Kithkin Soldier tokens are 1/1")
    void kithkinSoldierTokensAreOneOne() {
        castAndResolveGuardian();

        Permanent token = findKithkinSoldierToken(player1);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Kithkin gains 1 life and moves it to graveyard")
    void sacrificeKithkinGainsLife() {
        addGuardianReady(player1);
        addKithkin(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.getLife(player1.getId());

        // Only one Kithkin → auto-sacrifice
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getSubtypes().contains(CardSubtype.KITHKIN));
    }

    @Test
    @DisplayName("Ability requires {G} mana to activate")
    void abilityRequiresGreenMana() {
        addGuardianReady(player1);
        addKithkin(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Kithkin to sacrifice")
    void cannotActivateWithoutKithkin() {
        addGuardianReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature Kithkin permanent can pay the sacrifice cost")
    void canSacrificeKithkinEnchantment() {
        harness.addToBattlefield(player1, new GuardianOfCloverdell());
        harness.addToBattlefield(player1, new MilitiasPride());
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof MilitiasPride);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An opponent's Kithkin cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsKithkin() {
        harness.addToBattlefield(player1, new GuardianOfCloverdell());
        harness.addToBattlefield(player2, new KnightOfMeadowgrain());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A newly entered Guardian can sacrifice its own token for life")
    void canSacrificeTokenWhileSummoningSick() {
        castAndResolveGuardian();
        Permanent token = findKithkinSoldierToken(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castAndResolveGuardian() {
        harness.setHand(player1, List.of(new GuardianOfCloverdell()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent addGuardianReady(Player player) {
        return addCreatureReady(player, new GuardianOfCloverdell());
    }

    private void addKithkin(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new KnightOfMeadowgrain());
        }
    }

    private int countKithkinSoldierTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private Permanent findKithkinSoldierToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Kithkin Soldier token found"));
    }
}

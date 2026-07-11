package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuardianOfCloverdellTest extends BaseCardTest {

    // ===== ETB: creates three Kithkin Soldier tokens =====

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
    }

    // ===== Activated ability: {G}, Sacrifice a Kithkin: gain 1 life =====

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

    // ===== Helpers =====

    private void castAndResolveGuardian() {
        harness.setHand(player1, List.of(new GuardianOfCloverdell()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addGuardianReady(Player player) {
        Permanent guardian = new Permanent(new GuardianOfCloverdell());
        guardian.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(guardian);
        return guardian;
    }

    private void addKithkin(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent kithkin = new Permanent(createKithkinCard("Test Kithkin " + i));
            kithkin.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(kithkin);
        }
    }

    private Card createKithkinCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.KITHKIN));
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return card;
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

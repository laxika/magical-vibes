package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DruidsCallTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the enchanted creature creates that many Squirrel tokens")
    void damageCreatesTokensEqualToDamage() {
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, giant.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        List<Permanent> squirrels = squirrelTokens(player2);
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
        });
        assertThat(squirrelTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("The trigger still creates tokens when lethal damage removes the Aura and creature")
    void triggerResolvesAfterEnchantedCreatureDies() {
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, giant.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(squirrelTokens(player2)).hasSize(3);
        assertThat(squirrelTokens(player1)).isEmpty();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Druid's Call");
    }

    @Test
    @DisplayName("Druid's Call cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.s.Swamp());
        Permanent swamp = findPermanent(player1, "Swamp");
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private List<Permanent> squirrelTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Squirrel"))
                .toList();
    }
}

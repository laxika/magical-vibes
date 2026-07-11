package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HearthcageGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 3/1 Elemental Shaman tokens")
    void etbCreatesTwoElementalShamanTokens() {
        harness.setHand(player1, List.of(new HearthcageGiant()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elemental Shaman"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(3);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN);
                });
    }

    @Test
    @DisplayName("Sacrificing an Elemental gives target Giant +3/+1")
    void sacrificeElementalBoostsGiant() {
        Permanent giant = addHearthcageGiantReady(player1);
        harness.addToBattlefield(player1, createElementalToken());

        // The only Elemental is the token -> auto-sacrificed; the Giant targets itself.
        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elemental"));
        assertThat(giant.getEffectivePower()).isEqualTo(8);
        assertThat(giant.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent giant = addHearthcageGiantReady(player1);
        harness.addToBattlefield(player1, createElementalToken());

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        assertThat(giant.getEffectivePower()).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(5);
        assertThat(giant.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate with no Elemental to sacrifice")
    void cannotActivateWithoutElemental() {
        Permanent giant = addHearthcageGiantReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, giant.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Giant creature")
    void cannotTargetNonGiant() {
        addHearthcageGiantReady(player1);
        harness.addToBattlefield(player1, createElementalToken());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, createBearToken());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, bear.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addHearthcageGiantReady(Player player) {
        Permanent perm = new Permanent(new HearthcageGiant());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createElementalToken() {
        Card card = new Card();
        card.setName("Elemental");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.RED);
        card.setPower(3);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELEMENTAL, CardSubtype.SHAMAN));
        return card;
    }

    private Card createBearToken() {
        Card card = new Card();
        card.setName("Bear");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}

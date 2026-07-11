package com.github.laxika.magicalvibes.cards.m;

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

class MarshFlitterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 Goblin Rogue tokens")
    void etbCreatesTwoGoblinRogueTokens() {
        harness.setHand(player1, List.of(new MarshFlitter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Goblin Rogue"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
                });
    }

    @Test
    @DisplayName("Sacrificing a Goblin sets base power and toughness to 3/3")
    void sacrificeGoblinSetsBaseThreeThree() {
        Permanent flitter = addMarshFlitterReady(player1);
        harness.addToBattlefield(player1, createGoblinToken());

        // Marsh Flitter is a Faerie, so the only Goblin available is the token -> auto-sacrificed.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin"));
        assertThat(flitter.getEffectivePower()).isEqualTo(3);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Base power/toughness override resets at end of turn")
    void baseOverrideResetsAtEndOfTurn() {
        Permanent flitter = addMarshFlitterReady(player1);
        harness.addToBattlefield(player1, createGoblinToken());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(flitter.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flitter.getEffectivePower()).isEqualTo(1);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with no Goblin to sacrifice")
    void cannotActivateWithoutGoblin() {
        addMarshFlitterReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addMarshFlitterReady(Player player) {
        Permanent perm = new Permanent(new MarshFlitter());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createGoblinToken() {
        Card card = new Card();
        card.setName("Goblin");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        return card;
    }
}

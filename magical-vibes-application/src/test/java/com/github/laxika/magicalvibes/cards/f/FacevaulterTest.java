package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
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

class FacevaulterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Goblin gives +2/+2")
    void sacrificeAnotherGoblinGivesBoost() {
        addFacevaulterReady(player1);
        Permanent token = harness.addToBattlefieldAndReturn(player1, createGoblinToken());
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Two Goblins on battlefield (Facevaulter + token) → prompted to choose the sacrifice.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin"));

        Permanent facevaulter = findByName(gd, player1, "Facevaulter");
        assertThat(facevaulter.getPowerModifier()).isEqualTo(2);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Facevaulter can sacrifice itself for its own ability")
    void canSacrificeItself() {
        addFacevaulterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only Goblin on battlefield is Facevaulter → auto-sacrifice itself
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Facevaulter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Facevaulter"));
    }

    @Test
    @DisplayName("Ability requires {B} mana to activate")
    void abilityRequiresMana() {
        addFacevaulterReady(player1);
        harness.addToBattlefield(player1, createGoblinToken());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addFacevaulterReady(player1);
        Permanent token = harness.addToBattlefieldAndReturn(player1, createGoblinToken());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        Permanent facevaulter = findByName(gd, player1, "Facevaulter");
        assertThat(facevaulter.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(facevaulter.getPowerModifier()).isEqualTo(0);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addFacevaulterReady(Player player) {
        Facevaulter card = new Facevaulter();
        Permanent perm = new Permanent(card);
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

    private Permanent findByName(GameData gd, Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}

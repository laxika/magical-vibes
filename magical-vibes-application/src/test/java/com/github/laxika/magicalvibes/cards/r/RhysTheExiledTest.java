package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RhysTheExiledTest extends BaseCardTest {

    // ===== Attack trigger: gain 1 life per Elf you control =====

    @Test
    @DisplayName("Attacking gains 1 life when Rhys is the only Elf")
    void gainsOneLifeWhenOnlyElf() {
        addCreatureReady(player1, new RhysTheExiled());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 1);
    }

    @Test
    @DisplayName("Attacking gains 1 life for each Elf controlled, including non-attackers")
    void gainsLifePerElf() {
        addCreatureReady(player1, new RhysTheExiled());
        addCreatureReady(player1, createElf("Elf A"));
        addCreatureReady(player1, createElf("Elf B"));
        int startLife = gd.playerLifeTotals.get(player1.getId());

        // Only Rhys attacks; the other Elves stay back but still count.
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 3);
    }

    @Test
    @DisplayName("Non-Elf creatures do not add life")
    void nonElvesDoNotCount() {
        addCreatureReady(player1, new RhysTheExiled());
        addCreatureReady(player1, createNonElf("Goblin"));
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 1);
    }

    // ===== Activated ability: {B}, Sacrifice an Elf: Regenerate =====

    @Test
    @DisplayName("Activating the ability sacrifices an Elf and grants a regeneration shield")
    void activatingGrantsRegenerationShield() {
        Permanent rhys = addCreatureReady(player1, new RhysTheExiled());
        addToBattlefieldViaHarness(player1, createElf("Fodder Elf"));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID elfId = harness.getPermanentId(player1, "Fodder Elf");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elfId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fodder Elf"));
        assertThat(rhys.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability without {B}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RhysTheExiled());
        addToBattlefieldViaHarness(player1, createElf("Fodder Elf"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Helper methods =====

    private Card createElf(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private Card createNonElf(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private void addToBattlefieldViaHarness(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}

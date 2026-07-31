package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrionicResonatorTest extends BaseCardTest {

    private Permanent addReadyResonator() {
        Permanent perm = new Permanent(new StrionicResonator());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private int findBattlefieldIndex(Player player, String name) {
        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (name.equals(bf.get(i).getCard().getName())) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }

    private UUID triggeredAbilityCardIdOnStack() {
        return harness.getGameData().stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }

    @Test
    @DisplayName("Copies a triggered ability — target takes damage twice")
    void copiesTriggeredAbility() {
        harness.setLife(player2, 20);
        addReadyResonator();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerCardId = triggeredAbilityCardIdOnStack();
        harness.activateAbility(player1, findBattlefieldIndex(player1, "Strionic Resonator"), null, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Copy may be given a new target")
    void copyMayChooseNewTarget() {
        harness.setLife(player2, 20);
        addReadyResonator();
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerCardId = triggeredAbilityCardIdOnStack();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, findBattlefieldIndex(player1, "Strionic Resonator"), null, triggerCardId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target an opponent's triggered ability")
    void cannotTargetOpponentTrigger() {
        addReadyResonator();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TrialOfZeal()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castEnchantment(player2, 0, player1.getId());
        harness.passBothPriorities();

        UUID opponentTriggerCardId = triggeredAbilityCardIdOnStack();
        int resonatorIndex = findBattlefieldIndex(player1, "Strionic Resonator");
        assertThatThrownBy(() -> harness.activateAbility(player1, resonatorIndex, null, opponentTriggerCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        addReadyResonator();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);

        UUID spellCardId = harness.getGameData().stack.getLast().getCard().getId();
        int resonatorIndex = findBattlefieldIndex(player1, "Strionic Resonator");
        assertThatThrownBy(() -> harness.activateAbility(player1, resonatorIndex, null, spellCardId))
                .isInstanceOf(IllegalStateException.class);
    }
}

package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrochiSoulReaver.class, Forest.class, GrizzlyBears.class})
class OrochiSoulReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a Treasure and manifests the damaged player's top card under your control")
    void combatDamageCreatesTreasureAndManifestsDamagedPlayersTopCard() {
        addCreatureReady(player1, new OrochiSoulReaver());
        Card manifestedCard = new GrizzlyBears();
        Card remainder = new Forest();
        harness.setLibrary(player2, List.of(manifestedCard, remainder));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.isFaceDown()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainder);
    }

    @Test
    @DisplayName("Multiple creatures dealing combat damage create only one Treasure and manifest one card")
    void batchesMultipleCombatDamageDealers() {
        addCreatureReady(player1, new OrochiSoulReaver());
        addCreatureReady(player1, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(manifestedCard));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .map(permanent -> permanent.getCard().getId()))
                .containsExactly(manifestedCard.getId());
    }

    @Test
    @DisplayName("Ninjutsu puts Orochi Soul-Reaver onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OrochiSoulReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent orochi = findPermanent(player1, "Orochi Soul-Reaver");
        assertThat(orochi.isTapped()).isTrue();
        assertThat(orochi.isAttacking()).isTrue();
        assertThat(orochi.getAttackTarget()).isEqualTo(player2.getId());
    }
}

package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSoundOfDrums.class, GiantSpider.class, GrizzlyBears.class, ProdigalSorcerer.class, SerraAngel.class})
class TheSoundOfDrumsTest extends BaseCardTest {

    @Test
    @DisplayName("Goads the enchanted creature")
    void goadsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Doubles the enchanted creature's combat damage to a player")
    void doublesCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles the enchanted creature's combat damage to a permanent")
    void doublesCombatDamageToPermanent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, attacker);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double noncombat damage from the enchanted creature")
    void doesNotDoubleNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        attachAura(player1, sorcerer);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(sorcerer), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to its owner's hand")
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new TheSoundOfDrums()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "The Sound of Drums");
        harness.assertNotInGraveyard(player1, "The Sound of Drums");
    }

    @Test
    @DisplayName("Resolves as an Aura on an opponent's creature")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheSoundOfDrums()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "The Sound of Drums").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isGoaded(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Doubles an opponent's creature's combat damage even to the Aura controller")
    void doublesOpponentsCombatDamage() {
        harness.setLife(player1, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Two copies quadruple combat damage")
    void multipleAurasMultiplyDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        attachAura(player1, creature);

        declareAttackers(List.of(0));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Removing the Aura removes goad and damage doubling immediately")
    void auraLeavingEndsBothEffects() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachAura(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.isGoaded(gd, creature)).isFalse();
        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Doubles combat damage dealt by an enchanted blocker")
    void doublesBlockingCreaturesDamage() {
        Permanent attacker = addCreatureReady(player1, new SerraAngel());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        attachAura(player1, blocker);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Returns only the activated copy from the graveyard")
    void returnsOnlyActivatedCopy() {
        TheSoundOfDrums first = new TheSoundOfDrums();
        TheSoundOfDrums second = new TheSoundOfDrums();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, creature));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, creature);
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new TheSoundOfDrums());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}

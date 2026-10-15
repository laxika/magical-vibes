package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.ThermoAlchemist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Candletrap.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class, LightningBolt.class,
        ThermoAlchemist.class})
class CandletrapTest extends BaseCardTest {

    @Test
    @DisplayName("Candletrap gives the enchanted creature defender and prevents its combat damage")
    void defenderAndCombatDamagePrevention() {
        Permanent enchanted = addCreatureReady(player1, new HillGiant());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attachCandletrap(player1, enchanted);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DEFENDER)).isTrue();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(enchanted.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Candletrap does not prevent noncombat damage from the enchanted creature")
    void noncombatDamageStillApplies() {
        Permanent enchanted = addCreatureReady(player1, new ThermoAlchemist());
        attachCandletrap(player1, enchanted);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Coven sacrifice exiles the enchanted creature")
    void covenSacrificeExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        harness.assertInGraveyard(player1, "Candletrap");
    }

    @Test
    @DisplayName("Coven ability cannot be activated without three different powers")
    void covenRequiresDifferentPowers() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    void castAuraEnchantsOpponentCreatureAndStopsItAttacking() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Candletrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> enchanted.getId().equals(permanent.getAttachedTo()));
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchanted.isAttacking()).isFalse();
    }

    @Test
    void threeCreaturesWithOnlyTwoDifferentPowersDoNotMeetCoven() {
        Permanent enchanted = addCreatureReady(player2, new CrawWurm());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void losingCovenInResponseDoesNotStopExile() {
        Permanent enchanted = addCreatureReady(player2, new CrawWurm());
        Permanent aura = attachCandletrap(player1, enchanted);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DEFENDER)).isFalse();
        harness.assertInGraveyard(player1, "Candletrap");

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
    }

    @Test
    void creatureLeavingInResponseIsNotExiledFromGraveyard() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachCandletrap(player1, enchanted);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.castInstant(player2, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(enchanted.getCard());
    }

    private Permanent attachCandletrap(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Candletrap());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}

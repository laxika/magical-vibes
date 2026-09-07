package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterRealmOfPreservation.class, GrizzlyBears.class, HillGiant.class, Incinerate.class,
        OrcishArtillery.class, ScatheZombies.class})
class GreaterRealmOfPreservationTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next combat damage from a chosen black source and consumes the shield")
    void preventsBlackSourceDamage() {
        harness.setLife(player1, 20);
        addReadyRealm(player1);
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        zombie.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next combat damage from a chosen red source and consumes the shield")
    void preventsRedSourceDamage() {
        harness.setLife(player1, 20);
        addReadyRealm(player1);
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A source that is neither black nor red is not a valid choice")
    void nonBlackOrRedSourceNotValid() {
        addReadyRealm(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyRealm(player1);
        Permanent zombie = addCreatureReady(player2, new ScatheZombies());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen red source is prevented")
    void differentRedSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRealm(player1);
        Permanent chosen = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen red source")
    void preventsNextNoncombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyRealm(player1);
        Permanent artillery = addCreatureReady(player2, new OrcishArtillery());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artillery.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen red spell on the stack")
    void preventsNextDamageFromChosenRedSpell() {
        harness.setLife(player1, 20);
        addReadyRealm(player1);
        Incinerate incinerate = new Incinerate();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(incinerate.getId());

        harness.handlePermanentChosen(player1, incinerate.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyRealm(Player player) {
        Permanent perm = new Permanent(new GreaterRealmOfPreservation());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

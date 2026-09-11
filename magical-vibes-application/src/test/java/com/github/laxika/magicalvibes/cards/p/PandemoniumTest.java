package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RecklessOgre;
import com.github.laxika.magicalvibes.cards.w.WallOfNets;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pandemonium.class, RecklessOgre.class, WallOfNets.class, HillGiant.class})
class PandemoniumTest extends BaseCardTest {

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    @Test
    void enteringCreatureControllerMayHaveItDealItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        PendingInteraction.MayAbilityChoice mayChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveUntilInputOrEmpty();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void enteringCreatureMayDealItsPowerToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfNets());
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        harness.handlePermanentChosen(player1, target.getId());
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        resolveUntilInputOrEmpty();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void opponentCreatureControllerMakesTheChoice() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new RecklessOgre()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveUntilInputOrEmpty();

        PendingInteraction.MayAbilityChoice mayChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        resolveUntilInputOrEmpty();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void decliningTheMayAbilityDealsNoDamage() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RecklessOgre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void currentCreatureControllerChoosesWhetherToDealDamage() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        var creature = findPermanent(player1, "Hill Giant");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed(WurmcoilEngine.class)
    void enteringCreatureLifelinkGainsLifeForItsController() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WurmcoilEngine(), "{6}");
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player2, player1.getId());
        resolveUntilInputOrEmpty();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }

    @Test
    @CardUsed({WurmcoilEngine.class, Unsummon.class})
    void departedCreatureUsesLastKnownPowerLifelinkAndController() {
        harness.addToBattlefield(player1, new Pandemonium());
        harness.castFromHand(player1, new WurmcoilEngine(), "{6}");
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player1.getId());
        var creature = findPermanent(player1, "Wurmcoil Engine");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        resolveUntilInputOrEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }
}

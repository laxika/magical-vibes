package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlauthUnrivaledAncient.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class,
        GiantGrowth.class})
class KlauthUnrivaledAncientTest extends BaseCardTest {

    @Test
    void addsSpellOnlyPersistentManaEqualToAttackingPowerInAnyCombination() {
        addCreatureReady(player1, new KlauthUnrivaledAncient());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getSpellOnlyManaTotal()).isEqualTo(6);
        assertThat(pool.getTotal()).isEqualTo(6);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.getPersistentMana(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void spellOnlyManaCanCastSpellsButNotPayActivatedAbilities() {
        addCreatureReady(player1, new KlauthUnrivaledAncient());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        for (int i = 0; i < 6; i++) {
            harness.handleListChoice(player1, "RED");
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void calculatesAttackingPowerAfterACombatTrickResolves() {
        Permanent klauth = addCreatureReady(player1, new KlauthUnrivaledAncient());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, klauth.getId());
        resolveAllTriggers();
        chooseAllManaRed();

        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(9);
    }

    @Test
    void excludesAnAttackerDestroyedInResponseToTheTrigger() {
        addCreatureReady(player1, new KlauthUnrivaledAncient());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();
        chooseAllManaRed();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(4);
    }

    @Test
    void ignoresNonattackingCreaturesAndKeepsManaOnlyUntilEndOfTurn() {
        addCreatureReady(player1, new KlauthUnrivaledAncient());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        chooseAllManaRed();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).getSpellOnlyManaTotal()).isEqualTo(4);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void chooseAllManaRed() {
        for (int i = 0; i < 20 && gd.interaction.isAwaitingInput(); i++) {
            harness.handleListChoice(player1, "RED");
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}

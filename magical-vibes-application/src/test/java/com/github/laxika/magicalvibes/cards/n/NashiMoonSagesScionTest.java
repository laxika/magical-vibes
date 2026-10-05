package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AwakenedAwareness;
import com.github.laxika.magicalvibes.cards.d.DismalBackwater;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HinataDawnCrowned;
import com.github.laxika.magicalvibes.cards.m.MarchOfSwirlingMist;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NashiMoonSagesScion.class, GrizzlyBears.class, AwakenedAwareness.class,
        DismalBackwater.class, HinataDawnCrowned.class, MarchOfSwirlingMist.class})
class NashiMoonSagesScionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of each player's library")
    void combatDamageExilesEachLibraryTopCard() {
        Permanent nashi = addAttackingNashi();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(nashi.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("One exiled card may be cast by paying life equal to its mana value")
    void castsOneExiledCardForLife() {
        Permanent nashi = addAttackingNashi();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> nashi.getId().equals(entry.sourcePermanentId()))
                .toList();
        ExiledCardEntry first = exiled.getFirst();
        ExiledCardEntry second = exiled.get(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, first.card().getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.card().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ninjutsu puts Nashi onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NashiMoonSagesScion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.activateHandAbility(player1, 0, attacker.getId());
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent nashi = findPermanent(player1, "Nashi, Moon Sage's Scion");
        assertThat(nashi.isTapped()).isTrue();
        assertThat(nashi.isAttacking()).isTrue();
        assertThat(nashi.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void landPlayConsumesTheOneCardPermission() {
        addAttackingNashi();
        DismalBackwater land = new DismalBackwater();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(creature, new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dismal Backwater");
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void insufficientLifeDoesNotConsumePermission() {
        addAttackingNashi();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.setLife(player1, 20);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotChooseNonzeroXForTheLifeAlternative() {
        Permanent nashi = addAttackingNashi();
        AwakenedAwareness aura = new AwakenedAwareness();
        harness.setLibrary(player1, List.of(aura, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, aura.getId(), 3, nashi.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeAlternativeStillRequiresManaForHinatasTax() {
        Permanent nashi = addAttackingNashi();
        harness.addToBattlefield(player2, new HinataDawnCrowned());
        AwakenedAwareness aura = new AwakenedAwareness();
        harness.setLibrary(player1, List.of(aura, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();

        assertThatThrownBy(() -> harness.castFromExile(player1, aura.getId(), nashi.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void canCastMarchWithoutChoosingItsOptionalAdditionalCost() {
        addAttackingNashi();
        MarchOfSwirlingMist march = new MarchOfSwirlingMist();
        harness.setLibrary(player1, List.of(march, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();

        harness.castFromExile(player1, march.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "March of Swirling Mist");
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        addAttackingNashi();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void landCannotBePlayedAfterUsingTheNormalLandPlay() {
        addAttackingNashi();
        DismalBackwater land = new DismalBackwater();
        harness.setLibrary(player1, List.of(land, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        resolveCombatAndTrigger();
        preparePostcombatMain();
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
    }

    @Test
    void canCastOpponentsCardAfterNashiLeavesTheBattlefield() {
        Permanent nashi = addAttackingNashi();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(creature, new GrizzlyBears()));
        resolveCombatAndTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(nashi);
        gd.playerGraveyards.get(player1.getId()).add(nashi.getOriginalCard());
        preparePostcombatMain();

        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
    private void preparePostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
    private Permanent addAttackingNashi() {
        Permanent nashi = addCreatureReady(player1, new NashiMoonSagesScion());
        nashi.setAttacking(true);
        nashi.setAttackTarget(player2.getId());
        return nashi;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}

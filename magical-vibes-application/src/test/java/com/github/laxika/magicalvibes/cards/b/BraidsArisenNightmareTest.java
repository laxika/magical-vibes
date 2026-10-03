package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HithlainRope;
import com.github.laxika.magicalvibes.cards.j.JayaFieryNegotiator;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.r.RelicOfLegends;
import com.github.laxika.magicalvibes.cards.s.SalvagedManaworker;
import com.github.laxika.magicalvibes.cards.t.ThornbiteStaff;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsArisenNightmare.class, NishobaBrawler.class, RelicOfLegends.class, LightningStrike.class,
        SalvagedManaworker.class, Bitterblossom.class, ThornbiteStaff.class, HithlainRope.class,
        AshayaSoulOfTheWild.class, Forest.class, JayaFieryNegotiator.class})
class BraidsArisenNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent may sacrifice a permanent sharing a type with the sacrificed permanent")
    void opponentSacrificesMatchingPermanent() {
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        harness.addToBattlefield(player2, new RelicOfLegends());
        harness.addToBattlefield(player2, new NishobaBrawler());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Relic of Legends");
        harness.assertInGraveyard(player2, "Relic of Legends");
        harness.assertOnBattlefield(player2, "Nishoba Brawler");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent who declines causes Braids's controller to draw")
    void opponentDeclinesAndControllerDraws() {
        LightningStrike drawn = new LightningStrike();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        harness.addToBattlefield(player2, new RelicOfLegends());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player2, "Relic of Legends");
    }

    @Test
    @DisplayName("An opponent without a matching type causes Braids's controller to draw")
    void noMatchingPermanentDraws() {
        LightningStrike drawn = new LightningStrike();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        harness.addToBattlefield(player2, new NishobaBrawler());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertOnBattlefield(player2, "Nishoba Brawler");
    }

    @Test
    @DisplayName("Declining the initial sacrifice does nothing")
    void declinesInitialSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        harness.addToBattlefield(player2, new RelicOfLegends());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature allows an opponent to sacrifice an artifact")
    void artifactCreatureAllowsArtifactSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SalvagedManaworker());
        harness.addToBattlefield(player2, new RelicOfLegends());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Salvaged Manaworker");
        harness.assertInGraveyard(player2, "Relic of Legends");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An artifact creature allows an opponent to sacrifice a creature")
    void artifactCreatureAllowsCreatureSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new SalvagedManaworker());
        harness.addToBattlefield(player2, new NishobaBrawler());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Nishoba Brawler");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Braids can sacrifice herself and still finish resolving her ability")
    void sacrificesHerselfAndDraws() {
        LightningStrike drawn = new LightningStrike();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent braids = harness.addToBattlefieldAndReturn(player1, new BraidsArisenNightmare());
        harness.addToBattlefield(player2, new NishobaBrawler());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, braids.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Braids, Arisen Nightmare");
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Braids does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Kindred is sufficient to share a card type between an enchantment and an artifact")
    void kindredTypeAllowsArtifactSacrifice() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.addToBattlefield(player2, new ThornbiteStaff());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.assertInGraveyard(player2, "Thornbite Staff");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A permanent that cannot be sacrificed is excluded from the initial choice")
    void cannotChooseUnsacrificeablePermanent() {
        Permanent braids = harness.addToBattlefieldAndReturn(player1, new BraidsArisenNightmare());
        Permanent rope = harness.addToBattlefieldAndReturn(player1, new HithlainRope());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(braids.getId()).doesNotContain(rope.getId());
    }

    @Test
    @DisplayName("A sacrificed creature retains its continuously granted land type for matching")
    void usesLastKnownContinuouslyGrantedTypes() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new NishobaBrawler());
        harness.addToBattlefield(player2, new Forest());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a land allows an opponent to sacrifice a land")
    void landSacrificeMatchesLand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a planeswalker allows an opponent to sacrifice a planeswalker")
    void planeswalkerSacrificeMatchesPlaneswalker() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.enterBattlefieldAndReturn(player1, new JayaFieryNegotiator());
        harness.enterBattlefieldAndReturn(player2, new JayaFieryNegotiator());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Jaya, Fiery Negotiator");
        harness.assertInGraveyard(player2, "Jaya, Fiery Negotiator");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void resolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}

package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.cards.m.Meekstone;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.r.RayOfDistortion;
import com.github.laxika.magicalvibes.cards.w.Werebear;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarmicJustice.class, Demolish.class, DoomBlade.class, Firebolt.class, Forest.class,
        Meekstone.class, NevinyrralsDisk.class, PlanarCleansing.class, RayOfDistortion.class,
        Werebear.class})
class KarmicJusticeTest extends BaseCardTest {

    private void resolveDemolish(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Demolish()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(caster, 0, 0, targetId);
    }

    @Test
    @DisplayName("Opponent destruction triggers a may-destroy ability for their permanent")
    void opponentDestroysNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Werebear());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        UUID bearsId = harness.getPermanentId(player2, "Werebear");

        resolveDemolish(player2, forestId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Werebear");
    }

    @Test
    @DisplayName("Declining the triggered ability does not destroy the target")
    void mayBeDeclined() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Werebear());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        UUID bearsId = harness.getPermanentId(player2, "Werebear");

        resolveDemolish(player2, forestId);

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Werebear");
    }

    @Test
    @DisplayName("Destroying Karmic Justice itself still triggers its ability")
    void destroyingKarmicJusticeTriggersItsAbility() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player2, new Werebear());
        harness.setHand(player2, List.of(new RayOfDistortion()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID karmicJusticeId = harness.getPermanentId(player1, "Karmic Justice");
        UUID bearsId = harness.getPermanentId(player2, "Werebear");
        harness.castAndResolveInstant(player2, 0, karmicJusticeId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player2, "Werebear");
    }

    @Test
    @DisplayName("Mass destruction also triggers Karmic Justice")
    void massDestructionTriggersKarmicJustice() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new PlanarCleansing()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A creature destroyed by damage does not trigger Karmic Justice")
    void creatureDestroyedByDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Werebear());
        harness.setHand(player2, List.of(new Firebolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player1, "Werebear");
        harness.castAndResolveInstant(player2, 0, bearsId);

        harness.assertInGraveyard(player1, "Werebear");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature destroyed by an opponent's spell does not trigger Karmic Justice")
    void creatureDestroyedBySpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Werebear());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player1, "Werebear");
        harness.castAndResolveInstant(player2, 0, bearsId);

        harness.assertInGraveyard(player1, "Werebear");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A spell you control destroying your noncreature permanent does not trigger Karmic Justice")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        resolveDemolish(player1, forestId);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's activated ability destroying a noncreature permanent triggers Karmic Justice")
    void opponentAbilityDestroysNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player2, new NevinyrralsDisk());
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player2, "Nevinyrral's Disk");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Mass destruction triggers once for each noncreature permanent destroyed")
    void massDestructionTriggersForEachNoncreaturePermanent() {
        harness.addToBattlefield(player1, new KarmicJustice());
        harness.addToBattlefield(player1, new Meekstone());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new PlanarCleansing()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstForest.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondForest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Karmic Justice");
        harness.assertInGraveyard(player1, "Meekstone");
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Forest")))
                .hasSize(2);
    }
}

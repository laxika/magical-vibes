package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArgothianOpportunist;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KoilosRoc.class, ArgothianOpportunist.class, EnergyRefractor.class})
class KoilosRocTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a tapped Powerstone token")
    void etbCreatesTappedPowerstone() {
        createPowerstone();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        Permanent powerstone = powerstones.getFirst();
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new KoilosRoc(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Koilos Roc");
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new KoilosRoc());
        addCreatureReady(player2, new ArgothianOpportunist());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockRoc() {
        addCreatureReady(player1, new KoilosRoc());
        Permanent blocker = addCreatureReady(player2, new KoilosRoc());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enterTriggerResolvesAfterRocLeaves() {
        harness.castFromHand(player1, new KoilosRoc(), "{4}{U}");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Koilos Roc");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();

        Permanent roc = findPermanent(player1, "Koilos Roc");
        gd.playerBattlefields.get(player1.getId()).remove(roc);
        gd.playerGraveyards.get(player1.getId()).add(roc.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void powerstoneMustUntapBeforeProducingMana() {
        createPowerstone();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Powerstone"));
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);

        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerstoneManaCannotPayForNonartifactSpell() {
        createPowerstone();
        activatePowerstone();
        harness.setHand(player1, List.of(new KoilosRoc()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Koilos Roc");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerstoneManaCanPayForArtifactSpell() {
        createPowerstone();
        activatePowerstone();
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    private void createPowerstone() {
        harness.castFromHand(player1, new KoilosRoc(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void activatePowerstone() {
        harness.performUntapStep(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Powerstone"));
        harness.activateAbility(player1, index, null, null);
    }
}

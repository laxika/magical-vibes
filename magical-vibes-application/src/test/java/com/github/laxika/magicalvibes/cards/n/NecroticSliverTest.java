package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecroticSliver.class, SinewSliver.class, SerraSphinx.class, UrborgTombOfYawgmoth.class,
        ArtificialEvolution.class, Bitterblossom.class})
class NecroticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers gain the ability, including Necrotic Sliver and opposing Slivers")
    void grantsAbilityToAllSlivers() {
        Permanent necroticSliver = addCreatureReady(player1, new NecroticSliver());
        Permanent ownSliver = addCreatureReady(player1, new SinewSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SinewSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, necroticSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("A Sliver can sacrifice itself to destroy a target permanent")
    void sacrificesSliverAndDestroysTargetPermanent() {
        addCreatureReady(player1, new NecroticSliver());
        addCreatureReady(player1, new SinewSliver());
        Permanent target = addCreatureReady(player2, new SerraSphinx());

        activateAndResolve(player1, 1, target.getId());

        harness.assertNotOnBattlefield(player1, "Sinew Sliver");
        harness.assertInGraveyard(player1, "Sinew Sliver");
        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("The ability can destroy a noncreature permanent")
    void destroysNoncreaturePermanent() {
        addCreatureReady(player1, new NecroticSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());

        activateAndResolve(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Necrotic Sliver can use its own granted ability")
    void grantsAbilityToItself() {
        addCreatureReady(player1, new NecroticSliver());
        Permanent target = addCreatureReady(player2, new SerraSphinx());

        activateAndResolve(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Necrotic Sliver");
        harness.assertNotOnBattlefield(player1, "Necrotic Sliver");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("An opposing Sliver can use the granted ability")
    void opposingSliverCanUseGrantedAbility() {
        addCreatureReady(player1, new NecroticSliver());
        addCreatureReady(player2, new SinewSliver());
        Permanent target = addCreatureReady(player1, new SerraSphinx());

        activateAndResolve(player2, 0, target.getId());

        harness.assertInGraveyard(player2, "Sinew Sliver");
        harness.assertNotOnBattlefield(player2, "Sinew Sliver");
        harness.assertInGraveyard(player1, "Serra Sphinx");
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new NecroticSliver());
        Permanent nonSliver = addCreatureReady(player1, new SerraSphinx());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Necrotic Sliver leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new NecroticSliver());
        Permanent sliver = addCreatureReady(player1, new SinewSliver());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
    }

    @Test
    @DisplayName("An activated ability resolves after Necrotic Sliver leaves the battlefield")
    void activatedAbilityResolvesAfterGrantingSourceLeaves() {
        Permanent source = addCreatureReady(player1, new NecroticSliver());
        addCreatureReady(player1, new SinewSliver());
        Permanent target = addCreatureReady(player2, new SerraSphinx());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sinew Sliver");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("A noncreature Sliver permanent also gains the sacrifice ability")
    void grantsAbilityToNoncreatureSliver() {
        harness.addToBattlefield(player1, new NecroticSliver());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        activateAndResolve(player1, 1, target.getId());

        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sliver can activate the ability")
    void tappedSummoningSickSliverCanActivate() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NecroticSliver());
        harness.inMutationScope(() -> {
            source.setSummoningSick(true);
            source.tap();
        });
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());

        activateAndResolve(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Necrotic Sliver");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("The Sliver is sacrificed immediately even when it targets itself")
    void canTargetItselfAndPaysSacrificeBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NecroticSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, source.getId());

        harness.assertNotOnBattlefield(player1, "Necrotic Sliver");
        harness.assertInGraveyard(player1, "Necrotic Sliver");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Insufficient mana prevents activation without sacrificing the Sliver")
    void insufficientManaDoesNotSacrificeSliver() {
        harness.addToBattlefield(player1, new NecroticSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Necrotic Sliver");
        harness.assertOnBattlefield(player2, "Serra Sphinx");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void activateAndResolve(Player activatingPlayer, int permanentIndex, UUID targetId) {
        harness.forceActivePlayer(activatingPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(activatingPlayer, ManaColor.COLORLESS, 3);
        harness.activateAbility(activatingPlayer, permanentIndex, 0, null, targetId);
        harness.passBothPriorities();
    }
}

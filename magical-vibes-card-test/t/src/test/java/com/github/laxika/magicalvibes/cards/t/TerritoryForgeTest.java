package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritoryForge.class, Forest.class, GrizzlyBears.class, RodOfRuin.class, Commandeer.class})
class TerritoryForgeTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, exiles a target artifact and gains its activated abilities")
    void exilesArtifactAndGainsItsAbilities() {
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        Permanent forge = castForge(rod);

        assertThat(gd.getCardsExiledByPermanent(forge.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Rod of Ruin");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When cast, can target and exile a land")
    void exilesLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        Permanent forge = castForge(forest);

        assertThat(gd.getCardsExiledByPermanent(forge.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(findPermanents(player2, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("Can exile its controller's artifact and use its ability")
    void exilesOwnArtifactAndUsesAbility() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        castForge(rod);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Entering without being cast does not exile anything")
    void enteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player2, new RodOfRuin());

        Permanent forge = harness.enterBattlefieldAndReturn(player1, new TerritoryForge());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(forge.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Can immediately activate the mana ability of an exiled basic land")
    void gainsBasicLandManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent forge = castForge(forest);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TerritoryForge forge = new TerritoryForge();
        harness.setHand(player1, List.of(forge));
        addForgeMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    @DisplayName("Does not trigger when another player takes control of the spell")
    void stolenSpellDoesNotTriggerForNoncaster() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        TerritoryForge forge = new TerritoryForge();
        harness.setHand(player1, List.of(forge));
        addForgeMana();
        harness.castArtifact(player1, 0, forest.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castInstant(player2, 0, forge.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Territory Forge");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castForge(Permanent target) {
        harness.setHand(player1, List.of(new TerritoryForge()));
        addForgeMana();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Territory Forge");
    }

    private void addForgeMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}

package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoulderbranchGolem;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaticNet.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class StaticNetTest extends BaseCardTest {

    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StaticNet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles an opponent's nonland permanent, gains 2 life, and creates a tapped Powerstone")
    void etbEffects() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstones.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiled permanent returns when Static Net leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID staticNetId = harness.getPermanentId(player1, "Static Net");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, staticNetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StaticNet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StaticNet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Life gain and Powerstone creation happen even with no legal exile target")
    void gainsLifeAndCreatesPowerstoneWithoutExileTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new StaticNet(), "{3}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Static Net");
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
    }

    @Test
    @CardUsed({BoulderbranchGolem.class, Disenchant.class})
    @DisplayName("Removing the exile target does not stop the separate life and Powerstone ability")
    void illegalExileTargetDoesNotStopOtherAbility() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BoulderbranchGolem()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StaticNet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Boulderbranch Golem");
        harness.assertOnBattlefield(player1, "Static Net");
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
    }

    @Test
    @CardUsed({BoulderbranchGolem.class, Disenchant.class})
    @DisplayName("Removing Static Net before its abilities resolve prevents exile but not life or the token")
    void sourceLeavesBeforeAbilitiesResolve() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BoulderbranchGolem()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StaticNet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Static Net"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Static Net");
        harness.assertOnBattlefield(player2, "Boulderbranch Golem");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    @CardUsed({BoulderbranchGolem.class, Disenchant.class})
    @DisplayName("Powerstone mana pays for artifact spells but cannot pay for nonartifact spells")
    void powerstoneManaRestriction() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BoulderbranchGolem()).getId();
        castAndResolve(targetId);
        harness.performUntapStep(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int powerstoneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Powerstone"));
        harness.activateAbility(player1, powerstoneIndex, null, null);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID staticNetId = harness.getPermanentId(player1, "Static Net");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, staticNetId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Boulderbranch Golem");
        harness.assertLife(player1, 28);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
    }
}

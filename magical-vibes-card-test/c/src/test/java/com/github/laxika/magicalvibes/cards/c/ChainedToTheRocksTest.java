package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
import com.github.laxika.magicalvibes.cards.s.SatyrRambler;
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

@CardUsed({ChainedToTheRocks.class, Forest.class, Mountain.class, SatyrRambler.class, RayOfDissolution.class})
class ChainedToTheRocksTest extends BaseCardTest {

    @Test
    @DisplayName("Aura can resolve when no opponent controls a creature")
    void resolvesWithoutOpponentCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chained to the Rocks");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Chained to the Rocks"))
                .allMatch(p -> mountain.getId().equals(p.getAttachedTo()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura before its ETB resolves prevents exile")
    void auraLeavesBeforeTriggerResolves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Chained to the Rocks"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chained to the Rocks");
        assertThat(harness.getPermanentId(player2, "Satyr Rambler")).isEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creature target is chosen after the Aura enters")
    void choosesCreatureAfterAuraEnters() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chained to the Rocks");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Satyr Rambler");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Satyr Rambler"));
    }

    private void castAndResolve(UUID enchantTargetId, UUID exileTargetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, enchantTargetId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, exileTargetId);
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({Demolish.class})
    @DisplayName("Destroying the enchanted Mountain removes the Aura and returns the creature")
    void creatureReturnsWhenMountainIsDestroyed() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        castAndResolve(mountain.getId(), creature.getId());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Chained to the Rocks");
        harness.assertOnBattlefield(player2, "Satyr Rambler");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());

        castAndResolve(mountain.getId(), creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Satyr Rambler"));
    }

    @Test
    @DisplayName("Exiled creature returns when Chained to the Rocks leaves the battlefield")
    void exiledCreatureReturnsWhenAuraLeaves() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());

        castAndResolve(mountain.getId(), creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        UUID auraId = harness.getPermanentId(player1, "Chained to the Rocks");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, auraId);

        harness.assertOnBattlefield(player2, "Satyr Rambler");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Satyr Rambler"));
    }

    @Test
    @DisplayName("Cannot enchant a non-Mountain")
    void cannotEnchantNonMountain() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant an opponent's Mountain")
    void cannotEnchantOpponentsMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a creature you control")
    void cannotExileOwnCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        harness.setHand(player1, List.of(new ChainedToTheRocks()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Satyr Rambler")).isEqualTo(creature.getId());
    }
}

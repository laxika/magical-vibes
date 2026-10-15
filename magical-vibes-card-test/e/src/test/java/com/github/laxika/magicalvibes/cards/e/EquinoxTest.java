package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pyramids;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        Equinox.class, Forest.class, StoneRain.class, Armageddon.class,
        Naturalize.class, GrizzlyBears.class, HolyStrength.class,
        DarksteelCitadel.class, Pyramids.class
})
class EquinoxTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that would destroy a land you control")
    void countersTargetedLandDestruction() {
        Permanent forest = attachToForest();
        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, forest.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters a spell that would destroy another land you control")
    void countersDestructionOfAnotherControlledLand() {
        Permanent enchantedForest = attachToForest();
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, otherForest.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        assertThat(enchantedForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not counter a spell that destroys a nonland permanent")
    void doesNotCounterNonlandDestruction() {
        Permanent forest = attachToForest();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent holyStrength = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        holyStrength.setAttachedTo(bears.getId());

        Naturalize naturalize = new Naturalize();
        harness.setHand(player2, List.of(naturalize));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, holyStrength.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, naturalize.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Naturalize");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not counter a spell that would destroy an opponent's land")
    void doesNotCounterOpponentsLandDestruction() {
        Permanent forest = attachToForest();
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, opponentForest.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, stoneRain.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters a spell that would destroy all lands")
    void countersLandBoardWipe() {
        Permanent forest = attachToForest();
        Armageddon armageddon = new Armageddon();
        harness.setHand(player2, List.of(armageddon));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, armageddon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Armageddon");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lets the enchanted land's controller activate the ability")
    void abilityFollowsEnchantedLandsController() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent equinox = harness.addToBattlefieldAndReturn(player1, new Equinox());
        equinox.setAttachedTo(forest.getId());

        StoneRain stoneRain = new StoneRain();
        harness.setHand(player1, List.of(stoneRain));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, forest.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stone Rain");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    private Permanent attachToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent equinox = harness.addToBattlefieldAndReturn(player1, new Equinox());
        equinox.setAttachedTo(forest.getId());
        return forest;
    }

    @Test
    void doesNotCounterDestructionOfIndestructibleLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Equinox());
        aura.setAttachedTo(land.getId());
        StoneRain spell = new StoneRain();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, land.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertInGraveyard(player2, "Stone Rain");
    }

    @Test
    void doesNotCounterBoardWipeWhenOnlyControlledLandIsIndestructible() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Equinox());
        aura.setAttachedTo(land.getId());
        harness.addToBattlefield(player2, new Forest());
        Armageddon spell = new Armageddon();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void checksDestructionReplacementAtAbilityResolution() {
        Permanent land = attachToForest();
        harness.addToBattlefield(player1, new Pyramids());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        StoneRain spell = new StoneRain();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, land.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, spell.getId());
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "The next time target land would be destroyed this turn, remove all damage marked on it instead.");
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(land.getLandDestructionShield()).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(land.getLandDestructionShield()).isZero();
    }

    @Test
    void activatedAbilityStillCountersAfterAuraIsDestroyed() {
        Permanent land = attachToForest();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).get(1);
        StoneRain spell = new StoneRain();
        harness.setHand(player2, List.of(spell, new Naturalize()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, land.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, spell.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Equinox");
        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertOnBattlefield(player1, "Forest");
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlinkmothNexus;
import com.github.laxika.magicalvibes.cards.c.CelestialDawn;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.cards.l.LoxodonMystic;
import com.github.laxika.magicalvibes.cards.n.NettleDrone;
import com.github.laxika.magicalvibes.cards.v.VedalkenEngineer;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({MycosynthLattice.class, CrazedGoblin.class, BlinkmothNexus.class, LoxodonMystic.class,
        EchoingRuin.class, CelestialDawn.class, NettleDrone.class, VedalkenEngineer.class})
class MycosynthLatticeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every battlefield permanent an artifact and colorless")
    void makesBattlefieldPermanentsArtifactsAndColorless() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin());
        Permanent blinkmothNexus = harness.addToBattlefieldAndReturn(player1, new BlinkmothNexus());
        harness.addToBattlefield(player1, new MycosynthLattice());

        assertThat(gqs.isArtifact(gd, ownCreature)).isTrue();
        assertThat(gqs.isArtifact(gd, opponentCreature)).isTrue();
        assertThat(gqs.isCreature(gd, ownCreature)).isTrue();
        assertThat(gqs.isCreature(gd, opponentCreature)).isTrue();
        assertThat(gqs.isArtifact(gd, blinkmothNexus)).isTrue();
        assertThat(gqs.isLand(gd, blinkmothNexus)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, ownCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opponentCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, blinkmothNexus)).isEmpty();
    }

    @Test
    @DisplayName("Makes cards outside the battlefield colorless")
    void makesCardsOutsideBattlefieldColorless() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new CrazedGoblin()));

        assertThat(gqs.getEffectiveCardColors(gd, gd.playerHands.get(player1.getId()).getFirst()))
                .isEmpty();
    }

    @Test
    @DisplayName("Makes cards in every nonbattlefield zone colorless")
    void makesCardsInEveryNonBattlefieldZoneColorless() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        CrazedGoblin handCard = new CrazedGoblin();
        CrazedGoblin libraryCard = new CrazedGoblin();
        CrazedGoblin graveyardCard = new CrazedGoblin();
        CrazedGoblin exiledCard = new CrazedGoblin();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));

        assertThat(gqs.getEffectiveCardColors(gd, handCard)).isEmpty();
        assertThat(gqs.getEffectiveCardColors(gd, libraryCard)).isEmpty();
        assertThat(gqs.getEffectiveCardColors(gd, graveyardCard)).isEmpty();
        assertThat(gqs.getEffectiveCardColors(gd, exiledCard)).isEmpty();
    }

    @Test
    @DisplayName("Lets colored spells be cast using mana of any color")
    void castsColoredSpellWithAnyManaColor() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.castFromHand(player1, new CrazedGoblin(), "{U}");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Crazed Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("Makes a spell colorless and lets the opponent cast it with any color mana")
    void makesSpellColorlessAndLetsOpponentCastItWithAnyManaColor() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        CrazedGoblin spell = new CrazedGoblin();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, spell, "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveCardColors(gd, spell)).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Crazed Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("Lets colored activated abilities be paid with mana of any color")
    void letsColoredActivatedAbilitiesUseAnyManaColor() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless mana can pay a colored spell cost")
    void castsColoredSpellWithColorlessMana() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new CrazedGoblin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crazed Goblin");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The opponent can use colorless mana for a colored activated ability")
    void opponentPaysActivatedAbilityWithColorlessMana() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent target = addCreatureReady(player1, new CrazedGoblin());
        Permanent mystic = addCreatureReady(player2, new LoxodonMystic());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(mystic.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A creature resolving after Lattice becomes a colorless artifact")
    void affectsPermanentsEnteringAfterLattice() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        CrazedGoblin goblin = new CrazedGoblin();
        harness.castFromHand(player1, goblin, "{R}");

        assertThat(gqs.getEffectiveCardColors(gd, goblin)).isEmpty();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Crazed Goblin");
        assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        assertThat(gqs.isCreature(gd, permanent)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, permanent)).isEmpty();
    }

    @Test
    @DisplayName("Removing Lattice restores colors, types, and normal mana payment")
    void effectsStopAfterLatticeLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrazedGoblin());
        Permanent lattice = harness.addToBattlefieldAndReturn(player1, new MycosynthLattice());
        CrazedGoblin handCard = new CrazedGoblin();
        harness.setHand(player1, List.of(new EchoingRuin(), handCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, lattice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mycosynth Lattice");
        assertThat(gqs.isArtifact(gd, creature)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveCardColors(gd, handCard)).containsExactly(CardColor.RED);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Crazed Goblin");
    }

    @Test
    @CardUsed({MycosynthLattice.class, CelestialDawn.class, CrazedGoblin.class})
    @DisplayName("A later Celestial Dawn overrides Lattice for cards outside the battlefield")
    void laterColorSetterOverridesLatticeOutsideBattlefield() {
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        CrazedGoblin handCard = new CrazedGoblin();
        CrazedGoblin libraryCard = new CrazedGoblin();
        CrazedGoblin graveyardCard = new CrazedGoblin();
        CrazedGoblin exiledCard = new CrazedGoblin();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));
        harness.enterBattlefieldAndReturn(player1, new CelestialDawn());

        assertThat(gqs.getEffectiveCardColors(gd, handCard)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardColors(gd, libraryCard)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardColors(gd, graveyardCard)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardColors(gd, exiledCard)).containsExactly(CardColor.WHITE);
    }

    @Test
    @CardUsed({MycosynthLattice.class, CelestialDawn.class, CrazedGoblin.class})
    @DisplayName("A later Celestial Dawn overrides Lattice for its controller's spells")
    void laterColorSetterOverridesLatticeOnStack() {
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        harness.enterBattlefieldAndReturn(player1, new CelestialDawn());
        CrazedGoblin goblin = new CrazedGoblin();
        harness.castFromHand(player1, goblin, "{W}");

        assertThat(gqs.getEffectiveCardColors(gd, goblin)).containsExactly(CardColor.WHITE);
    }

    @Test
    @CardUsed({MycosynthLattice.class, CelestialDawn.class, CrazedGoblin.class})
    @DisplayName("A later Lattice overrides Celestial Dawn for cards outside the battlefield")
    void laterLatticeOverridesEarlierColorSetter() {
        harness.enterBattlefieldAndReturn(player1, new CelestialDawn());
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setHand(player1, List.of(goblin));
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());

        assertThat(gqs.getEffectiveCardColors(gd, goblin)).isEmpty();
    }

    @Test
    @CardUsed({MycosynthLattice.class, NettleDrone.class, CrazedGoblin.class})
    @DisplayName("A spell made colorless by Lattice triggers Nettle Drone")
    void colorlessSpellTriggersNettleDrone() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new NettleDrone());
        drone.tap();
        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lattice preserves artifact-only mana restrictions for nonartifact spells")
    void artifactRestrictedManaCannotCastNonartifactSpell() {
        addCreatureReady(player1, new VedalkenEngineer());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new CrazedGoblin()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Crazed Goblin");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact-restricted mana can activate a permanent made into an artifact by Lattice")
    void artifactRestrictedManaPaysForNewlyArtifactPermanent() {
        addCreatureReady(player1, new VedalkenEngineer());
        addCreatureReady(player1, new LoxodonMystic());
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.WHITE))
                .isEqualTo(1);
    }
}

package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlinkmothNexus;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.l.LoxodonMystic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MycosynthLattice.class, CrazedGoblin.class, BlinkmothNexus.class, LoxodonMystic.class})
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
        harness.setHand(player1, List.of(new CrazedGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Crazed Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("Makes a spell colorless and lets the opponent cast it with any color mana")
    void makesSpellColorlessAndLetsOpponentCastItWithAnyManaColor() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        CrazedGoblin spell = new CrazedGoblin();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

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
}

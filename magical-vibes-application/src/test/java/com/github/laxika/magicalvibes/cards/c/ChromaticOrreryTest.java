package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVisionary;
import com.github.laxika.magicalvibes.cards.l.LorescaleCoatl;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromaticOrrery.class, AlpineWatchdog.class, WalkingCorpse.class, OnakkeOgre.class,
        LlanowarVisionary.class, LorescaleCoatl.class, Forest.class, Shock.class, MycosynthLattice.class})
class ChromaticOrreryTest extends BaseCardTest {

    @Test
    @DisplayName("The tap ability adds five colorless mana")
    void tapAbilityAddsFiveColorlessMana() {
        harness.addToBattlefield(player1, new ChromaticOrrery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    @DisplayName("The draw ability draws for each distinct color among controlled permanents")
    void drawAbilityCountsDistinctControlledPermanentColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new OnakkeOgre());
        harness.addToBattlefield(player1, new LlanowarVisionary());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    @DisplayName("The static ability lets colorless mana pay a colored spell")
    void colorlessManaPaysColoredSpell() {
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.addToBattlefield(player2, new LlanowarVisionary());
        var targetId = harness.getPermanentId(player2, "Llanowar Visionary");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    void opponentCannotUseOrreryManaPermission() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.assertInHand(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void coloredManaCanPayADifferentColor() {
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void aGlobalManaPermissionStillAppliesToAnOpponentAlongsideOrrery() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void drawCountsMulticoloredPermanentsAndDuplicateColorsOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.addToBattlefield(player1, new LorescaleCoatl());
        harness.addToBattlefield(player1, new LlanowarVisionary());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new OnakkeOgre());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawWithOnlyColorlessPermanentsDrawsNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var orrery = harness.addToBattlefieldAndReturn(player1, new ChromaticOrrery());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LorescaleCoatl());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(orrery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void drawCountsColorsAtResolutionAfterAResponse() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ChromaticOrrery());
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawAbilityRequiresFiveMana() {
        harness.addToBattlefield(player1, new ChromaticOrrery());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void tappingForManaPreventsActivatingTheDrawAbility() {
        harness.addToBattlefield(player1, new ChromaticOrrery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }
}

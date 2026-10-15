package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraMutation;
import com.github.laxika.magicalvibes.cards.d.DevotedCaretaker;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrcishSpy;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SharaeOfNumbingDepths;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaidingParty.class, OrcishSpy.class, IcatianInfantry.class, RiverMerfolk.class,
        Plains.class, RayOfDistortion.class, DevotedCaretaker.class, AuraMutation.class,
        DreamThrush.class, Island.class, SharaeOfNumbingDepths.class})
class RaidingPartyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player taps white creatures and can preserve Plains they do not control")
    void tapsWhiteCreaturesAndPreservesChosenPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefieldAndReturn(player1, new OrcishSpy());
        Permanent player1White = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        Permanent player1TappedWhite = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        player1TappedWhite.tap();
        Permanent player1NonWhite = harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());
        Permanent player1Plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent player1OtherPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent player2White = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());
        Permanent player2Plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent player2OtherPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Tap =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Tap.playerId()).isEqualTo(player1.getId());
        assertThat(player1Tap.validIds()).containsExactly(player1White.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1White.getId()));

        PendingInteraction.MultiPermanentChoice player2Tap =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Tap.playerId()).isEqualTo(player2.getId());
        assertThat(player2Tap.validIds()).containsExactly(player2White.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2White.getId()));

        PendingInteraction.MultiPermanentChoice player1PlainsChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1PlainsChoice.playerId()).isEqualTo(player1.getId());
        assertThat(player1PlainsChoice.maxCount()).isEqualTo(2);
        assertThat(player1PlainsChoice.validIds()).containsExactlyInAnyOrder(
                player1Plains.getId(), player1OtherPlains.getId(),
                player2Plains.getId(), player2OtherPlains.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2Plains.getId()));

        PendingInteraction.MultiPermanentChoice player2PlainsInteraction =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2PlainsInteraction.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player1Plains.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1White.isTapped()).isTrue();
        assertThat(player2White.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Plains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1NonWhite);
        assertThat(player1TappedWhite.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Orcish Spy");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1OtherPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2OtherPlains);
    }

    @Test
    @DisplayName("Choosing no white creatures destroys all Plains")
    void choosingNoCreaturesDestroysAllPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefieldAndReturn(player1, new OrcishSpy());
        Permanent player1White = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        Permanent player2White = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(player1White.isTapped()).isFalse();
        assertThat(player2White.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Raiding Party cannot be activated without an Orc to sacrifice")
    void requiresAnOrcToSacrifice() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefieldAndReturn(player1, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White spells cannot target Raiding Party")
    void whiteSpellsCannotTargetIt() {
        Permanent raidingParty = harness.addToBattlefieldAndReturn(player2, new RaidingParty());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, raidingParty.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of white spells");
    }

    @Test
    @CardUsed(AuraMutation.class)
    @DisplayName("A multicolor white spell cannot target Raiding Party")
    void multicolorWhiteSpellCannotTargetIt() {
        Permanent raidingParty = harness.addToBattlefieldAndReturn(player2, new RaidingParty());
        harness.setHand(player1, List.of(new AuraMutation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, raidingParty.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }

    @Test
    @DisplayName("Abilities from white sources cannot target Raiding Party")
    void whiteAbilitiesCannotTargetIt() {
        Permanent raidingParty = harness.addToBattlefieldAndReturn(player1, new RaidingParty());
        Permanent devotedCaretaker = addCreatureReady(player1, new DevotedCaretaker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        int caretakerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(devotedCaretaker);
        assertThatThrownBy(() -> harness.activateAbility(player1, caretakerIndex, null, raidingParty.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white spells or abilities from white sources");
    }

    @Test
    void destroysALandThatBecameAPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefield(player1, new OrcishSpy());
        Permanent thrush = addCreatureReady(player1, new DreamThrush());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thrush),
                null, island.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island);
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    void preservesAPrintedPlainsThatBecameAnIsland() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefield(player1, new OrcishSpy());
        Permanent thrush = addCreatureReady(player1, new DreamThrush());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thrush),
                null, plains.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);
        harness.assertNotInGraveyard(player2, "Plains");
    }

    @Test
    void opponentsTappingTheirOwnCreaturesDoesNotTriggerSharae() {
        harness.addToBattlefield(player1, new RaidingParty());
        harness.addToBattlefield(player1, new OrcishSpy());
        harness.addToBattlefield(player1, new SharaeOfNumbingDepths());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());
        harness.addToBattlefield(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player2, List.of(whiteCreature.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(whiteCreature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    void twoWhiteCreaturesCanPreserveFourPlains() {
        harness.addToBattlefield(player1, new RaidingParty());
        Permanent orc = harness.addToBattlefieldAndReturn(player1, new OrcishSpy());
        orc.tap();
        Permanent firstWhite = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        Permanent secondWhite = harness.addToBattlefieldAndReturn(player1, new IcatianInfantry());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent thirdPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent fourthPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent unchosenPlains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstWhite.getId(), secondWhite.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstPlains.getId(), secondPlains.getId(),
                thirdPlains.getId(), fourthPlains.getId()));

        assertThat(firstWhite.isTapped()).isTrue();
        assertThat(secondWhite.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstPlains, secondPlains);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(thirdPlains, fourthPlains)
                .doesNotContain(unchosenPlains);
        harness.assertInGraveyard(player1, "Orcish Spy");
        harness.assertInGraveyard(player2, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}

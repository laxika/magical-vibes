package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ClaimOfErebos;
import com.github.laxika.magicalvibes.cards.n.NyxbornEidolon;
import com.github.laxika.magicalvibes.cards.p.PainSeer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OdunosRiverTrawler.class, NyxbornEidolon.class, PainSeer.class, ClaimOfErebos.class})
class OdunosRiverTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted enchantment creature card from the graveyard to hand")
    void etbReturnsEnchantmentCreatureToHand() {
        NyxbornEidolon eidolon = new NyxbornEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        castTrawler();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eidolon.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Eidolon");
        harness.assertNotInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    @DisplayName("ETB does not target a non-enchantment creature card")
    void etbRejectsNonEnchantmentCreature() {
        harness.setGraveyard(player1, List.of(new PainSeer()));

        castTrawler();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Pain Seer");
    }

    @Test
    @DisplayName("Activation sacrifices the creature and returns an enchantment creature card")
    void activationReturnsEnchantmentCreatureToHand() {
        OdunosRiverTrawler trawler = new OdunosRiverTrawler();
        NyxbornEidolon eidolon = new NyxbornEidolon();
        addCreatureReady(player1, trawler);
        harness.setGraveyard(player1, List.of(eidolon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(eidolon.getId()));
        harness.assertInGraveyard(player1, "Odunos River Trawler");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Eidolon");
        harness.assertNotInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    @DisplayName("Activation cannot target a non-enchantment creature card")
    void activationRejectsNonEnchantmentCreature() {
        addCreatureReady(player1, new OdunosRiverTrawler());
        PainSeer seer = new PainSeer();
        harness.setGraveyard(player1, List.of(seer));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(seer.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void etbRejectsNoncreatureEnchantment() {
        harness.setGraveyard(player1, List.of(new ClaimOfErebos()));

        castTrawler();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Claim of Erebos");
        harness.assertNotInHand(player1, "Claim of Erebos");
    }

    @Test
    void etbDoesNotUseOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new NyxbornEidolon()));

        castTrawler();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Nyxborn Eidolon");
        harness.assertNotInHand(player1, "Nyxborn Eidolon");
    }

    @Test
    void etbDoesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        NyxbornEidolon target = new NyxbornEidolon();
        NyxbornEidolon other = new NyxbornEidolon();
        harness.setGraveyard(player1, List.of(target, other));
        castTrawler();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Nyxborn Eidolon");
        harness.assertInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    void activationRejectsNoncreatureEnchantmentWithoutSacrificing() {
        ClaimOfErebos claim = new ClaimOfErebos();
        harness.addToBattlefield(player1, new OdunosRiverTrawler());
        harness.setGraveyard(player1, List.of(claim));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(claim.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Odunos River Trawler");
        harness.assertInGraveyard(player1, "Claim of Erebos");
    }

    @Test
    void activationRejectsOpponentsGraveyardWithoutSacrificing() {
        NyxbornEidolon eidolon = new NyxbornEidolon();
        harness.addToBattlefield(player1, new OdunosRiverTrawler());
        harness.setGraveyard(player2, List.of(eidolon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(eidolon.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Odunos River Trawler");
        harness.assertInGraveyard(player2, "Nyxborn Eidolon");
    }

    @Test
    void activationWorksWhileSummoningSickAndTapped() {
        NyxbornEidolon eidolon = new NyxbornEidolon();
        harness.addToBattlefield(player1, new OdunosRiverTrawler());
        findPermanent(player1, "Odunos River Trawler").tap();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(eidolon.getId()));
        harness.assertInGraveyard(player1, "Odunos River Trawler");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nyxborn Eidolon");
    }

    @Test
    void activationStillSacrificesWhenTargetLeavesBeforeResolution() {
        OdunosRiverTrawler trawler = new OdunosRiverTrawler();
        NyxbornEidolon eidolon = new NyxbornEidolon();
        harness.addToBattlefield(player1, trawler);
        harness.setGraveyard(player1, List.of(eidolon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(eidolon.getId()));

        harness.setGraveyard(player1, List.of(trawler));
        harness.setExile(player1, List.of(eidolon));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Odunos River Trawler");
        harness.assertNotInHand(player1, "Nyxborn Eidolon");
        harness.assertNotOnBattlefield(player1, "Odunos River Trawler");
    }

    private void castTrawler() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new OdunosRiverTrawler(), "{2}{B}");
        harness.passBothPriorities();
    }
}

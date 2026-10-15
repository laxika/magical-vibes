package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WakerOfWaves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionGrub.class, Forest.class, WalkingCorpse.class, ColossalDreadmaw.class,
        WakerOfWaves.class, Tarmogoyf.class})
class CarrionGrubTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 from the greatest creature-card power in its controller's graveyard")
    void boostsByGreatestCreaturePowerInOwnGraveyard() {
        Permanent grub = addCreatureReady(player1, new CarrionGrub());
        harness.setGraveyard(player1, List.of(
                new WalkingCorpse(), new ColossalDreadmaw(), new Forest()));
        harness.setGraveyard(player2, List.of(new WakerOfWaves()));

        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, grub)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card instanceof ColossalDreadmaw);

        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters by milling four cards")
    void entersByMillingFourCards() {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionGrub(), "{3}{B}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertOnBattlefield(player1, "Carrion Grub");
    }

    @Test
    void hasNoPowerBonusWithoutCreatureCardsInOwnGraveyard() {
        Permanent grub = addCreatureReady(player1, new CarrionGrub());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new WakerOfWaves()));
        assertThat(gqs.getEffectivePower(gd, grub)).isZero();

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, grub)).isZero();
    }

    @Test
    void millsAvailableCardsAndImmediatelyUsesTheirPower() {
        List<Card> library = List.of(new ColossalDreadmaw(), new Forest());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionGrub(), "{3}{B}");
        harness.passBothPriorities();

        Permanent grub = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, grub)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(6);
    }

    @Test
    void millsOnlyTheTopFourCards() {
        List<Card> library = List.of(new Forest(), new WalkingCorpse(), new Forest(),
                new Forest(), new ColossalDreadmaw());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionGrub(), "{3}{B}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(4));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyElementsOf(library.subList(0, 4));
        Permanent grub = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(2);
    }

    @Test
    void usesCharacteristicDefiningPowerOfCreatureCardsInGraveyard() {
        Permanent grub = addCreatureReady(player1, new CarrionGrub());
        harness.setGraveyard(player1, List.of(new Tarmogoyf()));
        harness.setGraveyard(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(2);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, grub)).isEqualTo(1);
    }
}

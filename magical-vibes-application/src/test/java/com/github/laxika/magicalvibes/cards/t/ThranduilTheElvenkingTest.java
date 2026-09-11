package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
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

@CardUsed({ThranduilTheElvenking.class, ElvishMystic.class, RodOfRuin.class,
        ThranduilSindarinLiege.class, GrizzlyBears.class})
class ThranduilTheElvenkingTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from Elf cards in its controller's graveyard")
    void gainsAbilitiesFromOwnElfGraveyard() {
        Permanent thranduil = addReadyThranduil();
        harness.setGraveyard(player1, List.of(new ElvishMystic()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(thranduil.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not gain abilities from non-Elf or opponent graveyard cards")
    void filtersGraveyardCards() {
        Permanent thranduil = addReadyThranduil();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.setGraveyard(player2, List.of(new ElvishMystic()));

        assertThat(gqs.computeStaticBonus(gd, thranduil).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("A legendary Elf entering under your control draws two then discards one")
    void legendaryElfEntryDrawsAndDiscards() {
        addReadyThranduil();
        GrizzlyBears drawnFirst = new GrizzlyBears();
        RodOfRuin drawnSecond = new RodOfRuin();
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.setHand(player1, List.of(new ThranduilSindarinLiege()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnSecond);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnFirst);
    }

    @Test
    @DisplayName("Thranduil's own entry does not trigger its draw ability")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new ThranduilTheElvenking()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyThranduil() {
        Permanent thranduil = harness.addToBattlefieldAndReturn(player1, new ThranduilTheElvenking());
        thranduil.setSummoningSick(false);
        return thranduil;
    }
}

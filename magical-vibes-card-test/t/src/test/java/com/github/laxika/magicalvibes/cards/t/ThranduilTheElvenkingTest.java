package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSingularity;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranduilTheElvenking.class, ElvishMystic.class, RodOfRuin.class,
        ThranduilSindarinLiege.class, GrizzlyBears.class, ElvishAberration.class,
        LeylineOfSingularity.class, ProwessOfTheFair.class})
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
        addReadyThranduil();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.setGraveyard(player2, List.of(new ElvishMystic()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent has no activated ability");
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
        harness.castFromHand(player1, new ThranduilTheElvenking(), "{2}{B}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsExplicitActivatedAbilityFromElf() {
        Permanent thranduil = addReadyThranduil();
        harness.setGraveyard(player1, List.of(new ElvishAberration()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(thranduil.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losesGrantedAbilityWhenElfLeavesGraveyard() {
        addReadyThranduil();
        harness.setGraveyard(player1, List.of(new ElvishMystic()));
        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent has no activated ability");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void grantedTapAbilityRespectsSummoningSickness() {
        harness.addToBattlefield(player1, new ThranduilTheElvenking());
        harness.setGraveyard(player1, List.of(new ElvishMystic()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Creature has summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void nonlegendaryElfEnteringDoesNotTrigger() {
        addReadyThranduil();

        harness.castFromHand(player1, new ElvishMystic(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentLegendaryElfEnteringDoesNotTrigger() {
        addReadyThranduil();

        harness.enterBattlefieldAndReturn(player2, new ThranduilSindarinLiege());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonElfEnteringDoesNotTrigger() {
        addReadyThranduil();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void legendaryNoncreatureElfEnteringDrawsAndDiscards() {
        addReadyThranduil();
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        GrizzlyBears first = new GrizzlyBears();
        RodOfRuin second = new RodOfRuin();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new ProwessOfTheFair(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    private Permanent addReadyThranduil() {
        Permanent thranduil = harness.addToBattlefieldAndReturn(player1, new ThranduilTheElvenking());
        thranduil.setSummoningSick(false);
        return thranduil;
    }
}

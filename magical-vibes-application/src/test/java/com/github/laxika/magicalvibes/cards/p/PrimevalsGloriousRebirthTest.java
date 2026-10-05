package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.k.KarnsTemporalSundering;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.n.NykthosShrineToNyx;
import com.github.laxika.magicalvibes.cards.o.OathOfTeferi;
import com.github.laxika.magicalvibes.cards.o.OnSerrasWings;
import com.github.laxika.magicalvibes.cards.t.TeferiHeroOfDominaria;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalsGloriousRebirth.class, ArvadTheCursed.class, BalothGorger.class,
        KarnsTemporalSundering.class, MoxAmber.class, OathOfTeferi.class,
        TeferiHeroOfDominaria.class, PurphorosGodOfTheForge.class, OnSerrasWings.class,
        NykthosShrineToNyx.class})
class PrimevalsGloriousRebirthTest extends BaseCardTest {

    private void castPrimevalsGloriousRebirth() {
        harness.castFromHand(player1, new PrimevalsGloriousRebirth(), "{5}{W}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new BalothGorger());
        assertThatThrownBy(() -> harness.castFromHand(player1,
                new PrimevalsGloriousRebirth(), "{5}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.castFromHand(player1, new PrimevalsGloriousRebirth(), "{5}{W}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName())
                .isEqualTo("Primevals' Glorious Rebirth");
    }

    @Test
    @DisplayName("Returns legendary creatures from graveyard to battlefield")
    void returnsLegendaryCreatures() {
        harness.addToBattlefield(player1, new TeferiHeroOfDominaria());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(new ArvadTheCursed()));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        harness.assertNotInGraveyard(player1, "Arvad the Cursed");
    }

    @Test
    @DisplayName("Returns legendary artifacts from graveyard to battlefield")
    void returnsLegendaryArtifacts() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(new MoxAmber()));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertNotInGraveyard(player1, "Mox Amber");
    }

    @Test
    @DisplayName("Returns legendary enchantments from graveyard to battlefield")
    void returnsLegendaryEnchantments() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(new OathOfTeferi()));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Oath of Teferi");
        harness.assertNotInGraveyard(player1, "Oath of Teferi");
    }

    @Test
    @DisplayName("Returns multiple legendary permanents at once")
    void returnsMultipleLegendaryPermanents() {
        harness.addToBattlefield(player1, new TeferiHeroOfDominaria());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(
                new ArvadTheCursed(),
                new MoxAmber(),
                new OathOfTeferi()
        ));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        harness.assertNotInGraveyard(player1, "Arvad the Cursed");
        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertOnBattlefield(player1, "Oath of Teferi");
        harness.assertNotInGraveyard(player1, "Mox Amber");
        harness.assertNotInGraveyard(player1, "Oath of Teferi");
    }

    @Test
    @DisplayName("Does not return non-legendary creatures")
    void doesNotReturnNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(new BalothGorger()));

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Baloth Gorger");
    }

    @Test
    @DisplayName("Does not return legendary sorceries (non-permanent cards)")
    void doesNotReturnLegendarySorceries() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(new KarnsTemporalSundering()));

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Karn's Temporal Sundering");
        harness.assertInGraveyard(player1, "Karn's Temporal Sundering");
    }

    @Test
    @DisplayName("Returns legendary permanents but leaves non-legendary and non-permanent cards")
    void selectiveReturn() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player1, List.of(
                new MoxAmber(), new BalothGorger(), new KarnsTemporalSundering()
        ));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertNotOnBattlefield(player1, "Karn's Temporal Sundering");
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Karn's Temporal Sundering");
        harness.assertNotInGraveyard(player1, "Mox Amber");
    }

    @Test
    @DisplayName("Works with empty graveyard")
    void worksWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castPrimevalsGloriousRebirth();

        // Only the spell itself should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Primevals' Glorious Rebirth"));
    }

    @Test
    @DisplayName("Does not return cards from opponent's graveyard")
    void doesNotReturnFromOpponentGraveyard() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setGraveyard(player2, List.of(new MoxAmber()));

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Mox Amber");
        harness.assertNotOnBattlefield(player2, "Mox Amber");
        harness.assertInGraveyard(player2, "Mox Amber");
    }

    @Test
    @DisplayName("A legendary artifact alone does not permit casting")
    void cannotCastWithOnlyLegendaryArtifact() {
        harness.addToBattlefield(player1, new MoxAmber());

        assertThatThrownBy(() -> harness.castFromHand(player1,
                new PrimevalsGloriousRebirth(), "{5}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's legendary creature does not permit casting")
    void cannotCastWithOpponentsLegendaryCreature() {
        harness.addToBattlefield(player2, new ArvadTheCursed());

        assertThatThrownBy(() -> harness.castFromHand(player1,
                new PrimevalsGloriousRebirth(), "{5}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A legendary planeswalker permits casting and planeswalkers return")
    void canCastWithLegendaryPlaneswalkerAndReturnPlaneswalker() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(new TeferiHeroOfDominaria()));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Teferi, Hero of Dominaria");
        harness.assertNotInGraveyard(player1, "Teferi, Hero of Dominaria");
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof ArvadTheCursed);
        castPrimevalsGloriousRebirth();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Teferi, Hero of Dominaria");
    }

    @Test
    @DisplayName("Losing the legendary creature after casting does not prevent resolution")
    void resolvesWithoutLegendaryCreatureStillOnBattlefield() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(new MoxAmber()));
        harness.castFromHand(player1, new PrimevalsGloriousRebirth(), "{5}{W}{B}");
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertNotInGraveyard(player1, "Mox Amber");
    }

    @Test
    @DisplayName("Returning trigger sources see creatures returning simultaneously")
    void returningPurphorosSeesReturningCreature() {
        harness.addToBattlefield(player1, new TeferiHeroOfDominaria());
        harness.setGraveyard(player1, List.of(new ArvadTheCursed(), new PurphorosGodOfTheForge()));

        castPrimevalsGloriousRebirth();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        harness.assertOnBattlefield(player1, "Purphoros, God of the Forge");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Returning legendary Auras attach to a chosen legal creature")
    void returnsLegendaryAuraAttachedToChosenCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(new OnSerrasWings()));

        castPrimevalsGloriousRebirth();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Arvad the Cursed"));

        harness.assertOnBattlefield(player1, "On Serra's Wings");
        harness.assertNotInGraveyard(player1, "On Serra's Wings");
        assertThat(findPermanent(player1, "On Serra's Wings").getAttachedTo())
                .isEqualTo(harness.getPermanentId(player1, "Arvad the Cursed"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns legendary lands to the battlefield")
    void returnsLegendaryLands() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(new NykthosShrineToNyx()));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Nykthos, Shrine to Nyx");
        harness.assertNotInGraveyard(player1, "Nykthos, Shrine to Nyx");
        assertThat(findPermanent(player1, "Nykthos, Shrine to Nyx").isTapped()).isFalse();
    }
}

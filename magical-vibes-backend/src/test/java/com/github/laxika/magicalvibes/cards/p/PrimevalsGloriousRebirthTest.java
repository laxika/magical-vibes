package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnsTemporalSundering;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.o.OathOfTeferi;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimevalsGloriousRebirthTest extends BaseCardTest {

    private void castPrimevalsGloriousRebirth() {
        harness.setHand(player1, new ArrayList<>(List.of(new PrimevalsGloriousRebirth())));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effect: return all legendary permanents from graveyard")
    void hasCorrectEffects() {
        PrimevalsGloriousRebirth card = new PrimevalsGloriousRebirth();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect effect = (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.returnAll()).isTrue();
        assertThat(effect.filter()).isInstanceOf(CardAllOfPredicate.class);

        CardAllOfPredicate allOf = (CardAllOfPredicate) effect.filter();
        assertThat(allOf.predicates()).hasSize(2);
        assertThat(allOf.predicates().get(0)).isInstanceOf(CardSupertypePredicate.class);
        assertThat(((CardSupertypePredicate) allOf.predicates().get(0)).supertype()).isEqualTo(CardSupertype.LEGENDARY);
        assertThat(allOf.predicates().get(1)).isInstanceOf(CardIsPermanentPredicate.class);
    }

    // ===== Legendary sorcery restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimevalsGloriousRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new PrimevalsGloriousRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName())
                .isEqualTo("Primevals' Glorious Rebirth");
    }

    // ===== Return legendary permanents =====

    @Test
    @DisplayName("Returns legendary creatures from graveyard to battlefield")
    void returnsLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card legendaryCreature = new ArvadTheCursed();
        gd.playerGraveyards.get(player1.getId()).add(legendaryCreature);

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Arvad the Cursed");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Arvad the Cursed"));
    }

    @Test
    @DisplayName("Returns legendary artifacts from graveyard to battlefield")
    void returnsLegendaryArtifacts() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card legendaryArtifact = new MoxAmber();
        gd.playerGraveyards.get(player1.getId()).add(legendaryArtifact);

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mox Amber"));
    }

    @Test
    @DisplayName("Returns legendary enchantments from graveyard to battlefield")
    void returnsLegendaryEnchantments() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card legendaryEnchantment = new OathOfTeferi();
        gd.playerGraveyards.get(player1.getId()).add(legendaryEnchantment);

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Oath of Teferi");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Oath of Teferi"));
    }

    @Test
    @DisplayName("Returns multiple legendary permanents at once")
    void returnsMultipleLegendaryPermanents() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new ArvadTheCursed(),
                new MoxAmber(),
                new OathOfTeferi()
        ));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertOnBattlefield(player1, "Oath of Teferi");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mox Amber"))
                .noneMatch(c -> c.getName().equals("Oath of Teferi"));
    }

    // ===== Filter: does not return non-matching cards =====

    @Test
    @DisplayName("Does not return non-legendary creatures")
    void doesNotReturnNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card nonLegendary = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(nonLegendary);

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not return legendary sorceries (non-permanent cards)")
    void doesNotReturnLegendarySorceries() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card legendarySorcery = new KarnsTemporalSundering();
        gd.playerGraveyards.get(player1.getId()).add(legendarySorcery);

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Karn's Temporal Sundering");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
    }

    @Test
    @DisplayName("Returns legendary permanents but leaves non-legendary and non-permanent cards")
    void selectiveReturn() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card legendaryArtifact = new MoxAmber();
        Card nonLegendaryCreature = new GrizzlyBears();
        Card legendarySorcery = new KarnsTemporalSundering();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                legendaryArtifact, nonLegendaryCreature, legendarySorcery
        ));

        castPrimevalsGloriousRebirth();

        harness.assertOnBattlefield(player1, "Mox Amber");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Karn's Temporal Sundering");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"))
                .noneMatch(c -> c.getName().equals("Mox Amber"));
    }

    // ===== Edge cases =====

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

        Card opponentLegendary = new MoxAmber();
        gd.playerGraveyards.get(player2.getId()).add(opponentLegendary);

        castPrimevalsGloriousRebirth();

        harness.assertNotOnBattlefield(player1, "Mox Amber");
        harness.assertNotOnBattlefield(player2, "Mox Amber");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mox Amber"));
    }
}

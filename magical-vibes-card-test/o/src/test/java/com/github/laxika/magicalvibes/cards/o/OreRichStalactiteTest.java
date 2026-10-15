package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CosmiumCatalyst;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyreticRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OreRichStalactite.class, CosmiumCatalyst.class, PyreticRitual.class, GrizzlyBears.class,
        FaithlessLooting.class})
class OreRichStalactiteTest extends BaseCardTest {

    @Test
    void restrictedManaPaysForASorcery() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        Card looting = new FaithlessLooting();
        harness.setHand(player1, List.of(looting));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(looting);
    }

    @Test
    void craftsWithAMixtureOfInstantsAndSorceries() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        List<Card> materials = List.of(new PyreticRitual(), new PyreticRitual(),
                new FaithlessLooting(), new FaithlessLooting());
        harness.setGraveyard(player1, materials);
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(findCatalyst().getId()))
                .containsExactlyInAnyOrderElementsOf(materials);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void restrictedManaPaysForAnInstant() {
        Permanent stalactite = harness.addToBattlefieldAndReturn(player1, new OreRichStalactite());
        harness.setHand(player1, List.of(new PyreticRitual()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stalactite.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void restrictedManaCannotPayForAnArtifact() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        harness.setHand(player1, List.of(new OreRichStalactite()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftsWithMoreThanFourMaterials() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        List<Card> rituals = List.of(new PyreticRitual(), new PyreticRitual(),
                new PyreticRitual(), new PyreticRitual(), new PyreticRitual());
        harness.setGraveyard(player1, rituals);
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, rituals.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(findCatalyst().getId()))
                .containsExactlyInAnyOrderElementsOf(rituals);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void craftCannotBeActivatedOutsideAMainPhase() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        harness.setGraveyard(player1, List.of(new PyreticRitual(), new PyreticRitual(),
                new PyreticRitual(), new PyreticRitual()));
        addCraftMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertOnBattlefield(player1, "Ore-Rich Stalactite");
    }

    @Test
    @DisplayName("Craft exiles four red instants or sorceries from the graveyard and transforms")
    void craftsWithRedInstantOrSorceryCardsFromGraveyard() {
        Permanent stalactite = harness.addToBattlefieldAndReturn(player1, new OreRichStalactite());
        List<Card> rituals = List.of(
                new PyreticRitual(), new PyreticRitual(), new PyreticRitual(), new PyreticRitual());
        Card nonMatching = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(rituals.get(0), rituals.get(1), rituals.get(2), rituals.get(3), nonMatching));
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent catalyst = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed() && permanent.getCard() instanceof CosmiumCatalyst)
                .findFirst()
                .orElseThrow();
        assertThat(gd.getCardsExiledByPermanent(catalyst.getId()))
                .containsExactlyInAnyOrderElementsOf(rituals);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonMatching);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalactite);
    }

    @Test
    @DisplayName("Craft requires four matching red instant or sorcery cards")
    void craftRejectsNonMatchingMaterials() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        harness.setGraveyard(player1, List.of(
                new PyreticRitual(), new PyreticRitual(), new PyreticRitual(), new GrizzlyBears()));
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 4");
    }

    @Test
    @DisplayName("Cosmium Catalyst offers one random crafted card to cast for free")
    void offersOneRandomCraftedCard() {
        craftIntoCatalyst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int catalystIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findCatalyst());
        harness.activateAbility(player1, catalystIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining Cosmium Catalyst's random card does not offer another card")
    void decliningRandomCardDoesNotOfferAnother() {
        craftIntoCatalyst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int catalystIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findCatalyst());
        harness.activateAbility(player1, catalystIndex, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void craftIntoCatalyst() {
        harness.addToBattlefield(player1, new OreRichStalactite());
        harness.setGraveyard(player1, List.of(
                new PyreticRitual(), new PyreticRitual(), new PyreticRitual(), new PyreticRitual()));
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private Permanent findCatalyst() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed() && permanent.getCard() instanceof CosmiumCatalyst)
                .findFirst()
                .orElseThrow();
    }
}

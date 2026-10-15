package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckAndRebuild.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Millstone.class})
class WreckAndRebuildTest extends BaseCardTest {

    @Test
    void destroysArtifactOrEnchantment() {
        Millstone millstone = new Millstone();
        harness.addToBattlefield(player2, millstone);
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 0, List.of(findPermanent(player2, "Millstone").getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void destroysEnchantment() {
        GloriousAnthem anthem = new GloriousAnthem();
        harness.addToBattlefield(player2, anthem);
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 0, List.of(findPermanent(player2, "Glorious Anthem").getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void rejectsCreatureForDestructionMode() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(findPermanent(player2, "Grizzly Bears").getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsFiveThenMayReturnLandTapped() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleGraveyardCardChosen(player1, 0);

        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(5);
    }

    @Test
    void mayDeclineReturningLand() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }

    @Test
    void flashbackUsesTheSameModesAndExilesTheSpell() {
        WreckAndRebuild spell = new WreckAndRebuild();
        harness.setGraveyard(player1, List.of(spell));
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, 0, findPermanent(player2, "Millstone").getId());
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void returnsFreshlyMilledLandDuringTheSpellResolution() {
        Forest land = new Forest();
        GrizzlyBears sixthCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(land, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), sixthCard));
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixthCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Wreck and Rebuild");
    }

    @Test
    void returnsPreexistingLandEvenWhenFewerThanFiveCardsCanBeMilled() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Wreck and Rebuild");
    }

    @Test
    void flashbackMillModeReturnsLandBeforeTheSpellFinishes() {
        WreckAndRebuild spell = new WreckAndRebuild();
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(spell, land));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.handleListChoice(player1,
                "Mill five cards, then you may put a land card from your graveyard onto the battlefield tapped.");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell, land);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void canReturnLandFromAnEmptyLibrary() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WreckAndRebuild()));
        addNormalMana();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Wreck and Rebuild");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}

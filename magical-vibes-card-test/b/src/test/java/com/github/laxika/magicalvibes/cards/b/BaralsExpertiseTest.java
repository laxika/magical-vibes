package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SweatworksBrawler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaralsExpertise.class, GrizzlyBears.class, Spellbook.class, SerraAngel.class,
        LlanowarWastes.class, SweatworksBrawler.class})
class BaralsExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three target artifacts and creatures to their owners' hands")
    void returnsUpToThreeArtifactsAndCreatures() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        List<UUID> targetIds = List.of(
                harness.getPermanentId(player1, "Spellbook"),
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Spellbook"));

        castExpertise(List.of(new BaralsExpertise()), targetIds);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gameData.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Spellbook"))
                .hasSize(1);
        assertThat(gameData.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears") || card.getName().equals("Spellbook"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Can choose no permanents to return")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new BaralsExpertise()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetInvalidPermanent() {
        harness.addToBattlefield(player2, new LlanowarWastes());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Wastes");
        harness.setHand(player1, List.of(new BaralsExpertise()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Offers a spell with mana value four or less from hand for free")
    void castsSpellWithManaValueAtMostFourFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castExpertise(List.of(new BaralsExpertise(), bears), List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not offer a spell with mana value greater than four")
    void doesNotOfferSpellWithManaValueGreaterThanFour() {
        castExpertise(List.of(new BaralsExpertise(), new SerraAngel()), List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can decline the free spell after returning a creature")
    void canDeclineFreeSpell() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castExpertise(List.of(new BaralsExpertise(), new Spellbook()),
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Spellbook");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Baral's Expertise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast a creature just returned to its controller's hand")
    void canCastReturnedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castExpertise(List.of(new BaralsExpertise()),
                List.of(harness.getPermanentId(player1, "Grizzly Bears")));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast a spell with mana value exactly four without its colored mana")
    void canCastManaValueFourSpell() {
        castExpertise(List.of(new BaralsExpertise(), new SweatworksBrawler()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sweatworks Brawler");
        harness.assertNotInHand(player1, "Sweatworks Brawler");
    }

    @Test
    @DisplayName("Accepting one free spell removes the other offers")
    void castsOnlyOneSpell() {
        castExpertise(List.of(new BaralsExpertise(), new GrizzlyBears(), new Spellbook()), List.of());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lands in hand are not offered as free spells")
    void doesNotOfferLand() {
        castExpertise(List.of(new BaralsExpertise(), new LlanowarWastes()), List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Llanowar Wastes");
    }

    @Test
    @DisplayName("No free spell is offered when every target has left the battlefield")
    void allTargetsIllegalPreventsFreeCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BaralsExpertise(), new Spellbook()));
        addMana();
        harness.castSorcery(player1, 0,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Baral's Expertise");
    }

    @Test
    @DisplayName("A remaining legal target is returned and the free spell is still offered")
    void partialTargetLossStillAllowsFreeCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new BaralsExpertise(), new SweatworksBrawler()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creatureId, artifactId));

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(creatureId));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sweatworks Brawler");
    }

    private void castExpertise(List<Card> hand, List<UUID> targetIds) {
        harness.setHand(player1, hand);
        addMana();

        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}

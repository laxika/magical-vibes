package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DirgurIslandDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaelstromOfTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tappingForColorless() {
        harness.addToBattlefield(player1, new MaelstromOfTheSpiritDragon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana usable for Dragon spells")
    void restrictedManaCastsDragonSpell() {
        harness.addToBattlefield(player1, new MaelstromOfTheSpiritDragon());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(createCreature("Test Dragon", "{G}", CardSubtype.DRAGON)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.DRAGON, CardSubtype.OMEN))).isZero();
    }

    @Test
    @DisplayName("Second ability adds mana usable for Omen spells")
    void restrictedManaCastsOmenSpell() {
        harness.addToBattlefield(player1, new MaelstromOfTheSpiritDragon());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player1, List.of(new DirgurIslandDragon()));

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.DRAGON, CardSubtype.OMEN))).isZero();
    }

    @Test
    @DisplayName("Second ability cannot pay for a non-Dragon, non-Omen spell")
    void restrictedManaCannotCastOtherSpell() {
        harness.addToBattlefield(player1, new MaelstromOfTheSpiritDragon());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(createCreature("Test Elf", "{G}", CardSubtype.ELF)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability cannot pay for a Dragon's activated ability")
    void restrictedManaCannotPayActivatedAbility() {
        harness.addToBattlefield(player1, new MaelstromOfTheSpiritDragon());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addToBattlefield(player1, createCreatureWithAbility("Ability Dragon", CardSubtype.DRAGON));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Third ability sacrifices the land and searches a revealed Dragon into hand")
    void searchesForDragon() {
        MaelstromOfTheSpiritDragon land = new MaelstromOfTheSpiritDragon();
        Card nonDragon = new GrizzlyBears();
        Card dragon = new DirgurIslandDragon();
        Permanent landPermanent = harness.addToBattlefieldAndReturn(player1, land);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(nonDragon, dragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(landPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().cards()).containsExactly(dragon);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
    }

    private static Card createCreature(String name, String manaCost, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private static Card createCreatureWithAbility(String name, CardSubtype subtype) {
        Card card = createCreature(name, "{2}", subtype);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{G}", List.of(new GainLifeEffect(1)), "{G}: You gain 1 life."));
        return card;
    }
}

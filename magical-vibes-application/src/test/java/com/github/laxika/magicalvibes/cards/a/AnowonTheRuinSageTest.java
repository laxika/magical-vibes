package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnowonTheRuinSageTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep each player sacrifices a non-Vampire creature")
    void eachPlayerSacrificesNonVampireCreature() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent player1Creature = addCreature(player1, "Player 1 Creature");
        Permanent player1Vampire = addVampire(player1, "Player 1 Vampire");
        Permanent player2Creature = addCreature(player2, "Player 2 Creature");
        Permanent player2Vampire = addVampire(player2, "Player 2 Vampire");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(player1Creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(player2Creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(player1Vampire.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(player2Vampire.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent creature = addCreature(player2, "Creature");

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The player chooses which non-Vampire creature to sacrifice")
    void playerChoosesNonVampireCreature() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent first = addCreature(player2, "First Creature");
        Permanent second = addCreature(player2, "Second Creature");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Noncreatures and Vampires are not eligible")
    void noncreaturesAndVampiresAreNotEligible() {
        harness.addToBattlefield(player1, new AnowonTheRuinSage());
        Permanent vampire = addVampire(player2, "Vampire");
        Permanent noncreature = addNoncreature(player2, "Artifact");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(vampire, noncreature);
    }

    private Permanent addCreature(Player player, String name) {
        return addPermanent(player, name, List.of());
    }

    private Permanent addVampire(Player player, String name) {
        return addPermanent(player, name, List.of(CardSubtype.VAMPIRE));
    }

    private Permanent addNoncreature(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addPermanent(Player player, String name, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(subtypes);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}

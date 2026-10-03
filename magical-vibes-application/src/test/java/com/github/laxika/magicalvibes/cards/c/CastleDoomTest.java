package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CastleDoom.class, ChromaticStar.class, Spellbook.class})
class CastleDoomTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleDoom());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to artifact spells")
    void addsArtifactSpellOnlyMana() {
        harness.addToBattlefield(player1, new CastleDoom());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOnlyMana(ManaColor.RED)).isEqualTo(1);

        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Artifact-restricted mana cannot cast a nonartifact spell")
    void restrictedManaCannotCastNonartifactSpell() {
        harness.addToBattlefield(player1, new CastleDoom());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        Card creature = createCard("Test Creature", CardType.CREATURE, "{R}", CardColor.RED);
        creature.setPower(2);
        creature.setToughness(2);
        harness.setHand(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability sacrifices an artifact and creates a Doombot")
    void sacrificesArtifactAndCreatesDoombot() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleDoom());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        Permanent doombot = findPermanent(player1, "Doombot");
        assertThat(doombot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(doombot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(doombot.getCard().getSubtypes())
                .containsExactly(CardSubtype.ROBOT, CardSubtype.VILLAIN);
        assertThat(doombot.getEffectivePower()).isEqualTo(3);
        assertThat(doombot.getEffectiveToughness()).isEqualTo(3);
        assertThat(castle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Doombot ability is restricted to sorcery speed")
    void doombotAbilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new CastleDoom());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact-spell mana cannot pay for an artifact's activated ability")
    void restrictedManaCannotActivateArtifactAbility() {
        harness.addToBattlefield(player1, new CastleDoom());
        Permanent star = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(star.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Chromatic Star");
    }

    @Test
    @DisplayName("The artifact is sacrificed as a cost before the Doombot is created")
    void sacrificesArtifactBeforeResolution() {
        harness.addToBattlefield(player1, new CastleDoom());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Doombot");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        Permanent doombot = findPermanent(player1, "Doombot");
        assertThat(doombot.getCard().getColors()).isEmpty();
        assertThat(doombot.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Doombot")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Doombot");
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleDoom());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(castle.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Doombot ability cannot be activated during an opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new CastleDoom());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Doombot ability cannot be activated with a spell on the stack")
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new CastleDoom());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Card createCard(String name, CardType type, String manaCost, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        card.setColor(color);
        return card;
    }
}

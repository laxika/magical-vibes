package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ravenform.class, Forest.class, GrizzlyBears.class, Spellbook.class, Mistwalker.class})
class RavenformTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and gives its controller a blue Bird token")
    void exilesCreatureAndCreatesBirdForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRavenform(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertBirdToken(player2);
    }

    @Test
    @DisplayName("Exiles an artifact and gives its controller a Bird token")
    void exilesArtifactAndCreatesBirdForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        castRavenform(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spellbook"));
        assertBirdToken(player2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Ravenform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    void canExileOwnCreatureAndCreateBirdForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mistwalker());

        castRavenform(target);

        harness.assertNotOnBattlefield(player1, "Mistwalker");
        assertBirdToken(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void createsBirdForControllerRatherThanOwner() {
        Mistwalker creature = new Mistwalker();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);

        castRavenform(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertBirdToken(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotCreateBirdWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player1, List.of(new Ravenform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ravenform");
        harness.assertInGraveyard(player2, "Mistwalker");
    }

    @Test
    void foretellsFaceDownAndCastsForOneBlueOnLaterTurn() {
        Ravenform spell = new Ravenform();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(spell.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, spell.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertNotOnBattlefield(player2, "Mistwalker");
        assertBirdToken(player2);
        harness.assertInGraveyard(player1, "Ravenform");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotCastOnTurnItWasForetold() {
        Ravenform spell = new Ravenform();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        Ravenform spell = new Ravenform();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void foretellDoesNotAllowSorceryOnOpponentsTurn() {
        Ravenform spell = new Ravenform();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mistwalker());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castRavenform(Permanent target) {
        harness.setHand(player1, List.of(new Ravenform()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void assertBirdToken(com.github.laxika.magicalvibes.model.Player player) {
        assertThat(gd.playerBattlefields.get(player.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird")
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }
}

package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RavenousDemonTest extends BaseCardTest {

    

    @Test
    @DisplayName("Front face does not trigger during upkeep")
    void frontFaceDoesNotTriggerDuringUpkeep() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        addCreature(player1, createCreature("Doomed Human", CardSubtype.HUMAN));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isFalse();
        assertThat(demon.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Doomed Human"));
    }

    @Test
    @DisplayName("Activating front face sacrifices the only Human and transforms")
    void activatingSacrificesOnlyHumanAndTransforms() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        addCreature(player1, createCreature("Doomed Human", CardSubtype.HUMAN));
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isTrue();
        assertThat(demon.getCard().getName()).isEqualTo("Archdemon of Greed");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Doomed Human"));
    }

    @Test
    @DisplayName("Front activated ability ignores non-Human creatures")
    void frontActivationIgnoresNonHumanCreatures() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        addCreature(player1, createCreature("Bear", CardSubtype.BEAR));
        forceSorcerySpeed(player1);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(demon.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bear"));
    }

    @Test
    @DisplayName("Front activated ability prompts when multiple Humans are available")
    void frontActivationPromptsForMultipleHumans() {
        harness.addToBattlefield(player1, new RavenousDemon());
        Permanent first = addCreature(player1, createCreature("First Human", CardSubtype.HUMAN));
        Permanent second = addCreature(player1, createCreature("Second Human", CardSubtype.HUMAN));
        Permanent bear = addCreature(player1, createCreature("Bear", CardSubtype.BEAR));
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId())
                .doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("Chosen Human is sacrificed as activation cost and Ravenous Demon transforms")
    void chosenHumanActivationCostTransforms() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new RavenousDemon());
        Permanent first = addCreature(player1, createCreature("First Human", CardSubtype.HUMAN));
        addCreature(player1, createCreature("Second Human", CardSubtype.HUMAN));
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(demon.isTransformed()).isTrue();
        assertThat(demon.getCard().getName()).isEqualTo("Archdemon of Greed");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("First Human"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Second Human"));
    }

    @Test
    @DisplayName("Archdemon taps and deals 9 damage to controller when no Human is available")
    void archdemonTapsAndDealsDamageWithoutHuman() {
        Permanent archdemon = addTransformedArchdemon(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archdemon.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 9);
    }

    @Test
    @DisplayName("Archdemon sacrifices a Human without transforming back or dealing damage")
    void archdemonSacrificesHumanWithoutTransformingBack() {
        Permanent archdemon = addTransformedArchdemon(player1);
        addCreature(player1, createCreature("Doomed Human", CardSubtype.HUMAN));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archdemon.isTransformed()).isTrue();
        assertThat(archdemon.getCard().getName()).isEqualTo("Archdemon of Greed");
        assertThat(archdemon.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Doomed Human"));
    }

    @Test
    @DisplayName("Archdemon prompts for a Human when multiple Humans are available")
    void archdemonPromptsForMultipleHumans() {
        addTransformedArchdemon(player1);
        Permanent first = addCreature(player1, createCreature("First Human", CardSubtype.HUMAN));
        Permanent second = addCreature(player1, createCreature("Second Human", CardSubtype.HUMAN));
        Permanent bear = addCreature(player1, createCreature("Bear", CardSubtype.BEAR));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.ForcedCostOrElse.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId(), second.getId())
                .doesNotContain(bear.getId());
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void forceSorcerySpeed(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addTransformedArchdemon(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RavenousDemon());
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}

package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlongTheCrookedWay.class, Disentomb.class, GoblinAssailant.class, GrizzlyBears.class,
        LightningBolt.class, OrcishArtillery.class})
class AlongTheCrookedWayTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and returns a target creature card from your graveyard to your hand")
    void returnsTargetCreatureCardToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AlongTheCrookedWay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB targets only creature cards in your graveyard")
    void targetsOnlyOwnCreatureCards() {
        Card noncreature = new LightningBolt();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new AlongTheCrookedWay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaving your graveyard amasses Goblins 1")
    void amassesGoblinsWhenCreatureCardLeavesGraveyard() {
        addAlongTheCrookedWay();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Amass adds a Goblin subtype to an existing Army")
    void amassesOnExistingArmy() {
        addAlongTheCrookedWay();
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The activation grants menace only to your Goblins and Orcs until end of turn")
    void activationGrantsMenaceToGoblinsAndOrcs() {
        addAlongTheCrookedWay();
        Permanent goblin = addCreatureReady(player1, new GoblinAssailant());
        Permanent orc = addCreatureReady(player1, new OrcishArtillery());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingGoblin = addCreatureReady(player2, new GoblinAssailant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, orc, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGoblin, Keyword.MENACE)).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, orc, Keyword.MENACE)).isFalse();
    }

    private void addAlongTheCrookedWay() {
        harness.addToBattlefield(player1, new AlongTheCrookedWay());
    }
}
